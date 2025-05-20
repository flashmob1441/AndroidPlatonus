import uuid
from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select, func
from sqlalchemy.ext.asyncio import AsyncSession

from app import models
from app.db.session import get_session
from app.dependencies import get_current_teacher
from app.schemas import (
    GradesResponse,
    GradeHistoryResponse,
    SubmitGradeRequest,
    SubmitGradeResponse,
    GradeDto,
)

router = APIRouter(tags=["grades"])


async def _period_id(year: int, period: int, session: AsyncSession) -> str:
    q = await session.execute(
        select(models.AcademicPeriod).where(
            models.AcademicPeriod.year == year,
            models.AcademicPeriod.period == period,
        )
    )
    ap: models.AcademicPeriod | None = q.scalars().first()
    if ap is None:
        raise HTTPException(status_code=404, detail="Academic period not found")
    return ap.id


@router.get("/grades", response_model=GradesResponse)
async def grades(
    studentId: str,
    year: int,
    period: int,
    session: AsyncSession = Depends(get_session),
):
    period_id = await _period_id(year, period, session)

    avg_stmt = (
        select(
            models.Grade.subject_id,
            func.avg(models.Grade.score).label("avg_score"),
            func.max(models.Grade.date).label("last_date"),
        )
        .where(
            models.Grade.student_id == studentId,
            models.Grade.academic_period_id == period_id,
        )
        .group_by(models.Grade.subject_id)
    )
    rows = (await session.execute(avg_stmt)).all()

    result = []
    for subject_id, avg_score, last_date in rows:
        subject = await session.get(models.Subject, subject_id)

        teacher_name = (
            await session.execute(
                select(models.User.name)
                .join(models.Grade, models.Grade.teacher_id == models.User.id)
                .where(
                    models.Grade.student_id == studentId,
                    models.Grade.subject_id == subject_id,
                    models.Grade.academic_period_id == period_id,
                )
                .limit(1)
            )
        ).scalar_one()

        result.append(
            GradeDto(
                id=subject_id,
                studentId=studentId,
                subjectId=subject_id,
                subjectName=subject.name,
                teacherName=teacher_name,
                score=int(round(avg_score)),
                date=last_date,
                academicPeriodId=period_id,
                isFinal=False,
            )
        )
    return {"grades": result}


@router.get("/grades/history", response_model=GradeHistoryResponse)
async def grade_history(
    studentId: str,
    subjectId: str,
    year: int,
    period: int,
    session: AsyncSession = Depends(get_session),
):
    period_id = await _period_id(year, period, session)

    q = await session.execute(
        select(models.Grade).where(
            models.Grade.student_id == studentId,
            models.Grade.subject_id == subjectId,
            models.Grade.academic_period_id == period_id,
        )
    )
    items = q.scalars().all()
    history = []
    for g in items:
        subject = await session.get(models.Subject, g.subject_id)
        teacher = await session.get(models.User, g.teacher_id)
        history.append(
            GradeDto(
                id=g.id,
                studentId=g.student_id,
                subjectId=g.subject_id,
                subjectName=subject.name,
                teacherName=teacher.name,
                score=g.score,
                date=g.date,
                academicPeriodId=g.academic_period_id,
                isFinal=g.is_final,
            )
        )
    return {"history": history}


@router.post("/grades", response_model=SubmitGradeResponse)
async def submit_grade(
    data: SubmitGradeRequest,
    teacher: models.User = Depends(get_current_teacher),
    session: AsyncSession = Depends(get_session),
):
    if teacher.id != data.teacherId:
        raise HTTPException(status_code=403, detail="Forbidden")

    if await session.get(models.Subject, data.subjectId) is None:
        raise HTTPException(status_code=404, detail="Subject not found")

    lessons = await session.execute(
        select(models.Lesson).where(
            models.Lesson.subject_id == data.subjectId,
            models.Lesson.teacher_id == teacher.id,
        )
    )
    if lessons.first() is None:
        raise HTTPException(status_code=403, detail="Teacher not assigned to subject")

    grade_date = datetime.fromtimestamp(data.date // 1000)

    now_year, now_week, _ = datetime.utcnow().isocalendar()
    g_year, g_week, _ = grade_date.isocalendar()
    if (g_year, g_week) > (now_year, now_week):
        raise HTTPException(status_code=400, detail="Cannot set grade for future week")

    query = await session.execute(
        select(models.Grade).where(
            models.Grade.student_id == data.studentId,
            models.Grade.subject_id == data.subjectId,
            models.Grade.date == grade_date,
        )
    )
    existing: models.Grade | None = query.scalars().first()

    if (g_year, g_week) < (now_year, now_week) and existing is not None:
        raise HTTPException(status_code=400, detail="Cannot modify past grade")

    ap_id = await _period_id(year=g_year, period=2, session=session)

    if existing is None:
        session.add(
            models.Grade(
                id=str(uuid.uuid4()),
                student_id=data.studentId,
                teacher_id=teacher.id,
                subject_id=data.subjectId,
                score=data.score,
                date=grade_date,
                academic_period_id=ap_id,
                is_final=False,
            )
        )
    else:
        existing.score = data.score

    await session.commit()
    return {"success": True}

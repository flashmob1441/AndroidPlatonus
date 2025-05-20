from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from typing import Dict, List, Set, Tuple
from app.db.session import get_session
from app import models
from app.schemas import ScheduleResponse, Subject

router = APIRouter(tags=['schedule'])


@router.get('/schedule', response_model=ScheduleResponse)
async def schedule(userId: str, role: str, year: int, session: AsyncSession = Depends(get_session)):
    user: models.User | None = await session.get(models.User, userId)
    if user is None or user.role.value != role.lower():
        raise HTTPException(status_code=404, detail='User not found')

    if user.role == models.UserRole.teacher:
        stmt = (
            select(models.Lesson, models.Subject, models.User)
            .join(models.Subject, models.Subject.id == models.Lesson.subject_id)
            .join(models.User, models.User.id == models.Lesson.teacher_id)
            .where(models.Lesson.teacher_id == userId)
        )
    else:
        if user.group_id is None:
            raise HTTPException(status_code=400, detail='Student has no group')
        stmt = (
            select(models.Lesson, models.Subject, models.User)
            .join(models.Subject, models.Subject.id == models.Lesson.subject_id)
            .join(models.User, models.User.id == models.Lesson.teacher_id)
            .where(models.Lesson.group_id == user.group_id)
        )

    rows = (await session.execute(stmt)).all()
    days: Dict[int, List[Subject]] = {}
    seen: Set[Tuple[int, str]] = set()

    for lesson, subject, teacher in rows:
        key = (lesson.weekday, lesson.time) if user.role == models.UserRole.teacher else (lesson.id,)
        if key in seen:
            continue
        seen.add(key)

        dto = Subject(
            id=subject.id,
            name=subject.name,
            teacherId=teacher.id,
            teacherName=teacher.name,
            time=lesson.time,
            room=lesson.room,
            lessonType=lesson.lesson_type,
            dayOfWeek=lesson.weekday,
            weekNumber=None
        )
        days.setdefault(lesson.weekday, []).append(dto)

    return {'days': days}
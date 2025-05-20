from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.dependencies import get_current_teacher
from app.db.session import get_session
from app.schemas import StudentsResponse, User, GroupsResponse
from app import models

router = APIRouter(prefix='/teacher', tags=['teacher'])


@router.get('/groups', response_model=GroupsResponse)
async def groups(
    teacher: models.User = Depends(get_current_teacher),
    session: AsyncSession = Depends(get_session),
):
    q = await session.execute(
        select(models.Group)
        .join(models.Lesson, models.Lesson.group_id == models.Group.id)
        .where(models.Lesson.teacher_id == teacher.id)
        .distinct(models.Group.id)
    )
    groups = q.scalars().all()
    return {'groups': groups}


@router.get('/students', response_model=StudentsResponse)
async def students(
    groupId: str,
    teacher: models.User = Depends(get_current_teacher),
    session: AsyncSession = Depends(get_session),
):
    allowed = await session.execute(
        select(models.Lesson)
        .where(models.Lesson.teacher_id == teacher.id, models.Lesson.group_id == groupId)
        .limit(1)
    )
    if allowed.first() is None:
        raise HTTPException(status_code=403, detail='Forbidden')

    q = await session.execute(select(models.User).where(models.User.group_id == groupId))
    students = q.scalars().all()
    return {'students': [User.model_validate(s) for s in students]}
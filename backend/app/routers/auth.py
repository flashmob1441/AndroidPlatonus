import uuid

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from app.schemas import LoginRequest, Token, User, RegisterResponse, RegisterRequest
from app.db.session import get_session
from app.core.security import verify_password, create_access_token, get_password_hash
from app import models

router = APIRouter(prefix='/auth', tags=['auth'])


@router.post('/login', response_model=Token)
async def login(data: LoginRequest, session: AsyncSession = Depends(get_session)):
    query = await session.execute(select(models.User).where(models.User.email == data.email))
    user: models.User | None = query.scalars().first()
    if user is None or not verify_password(data.password, user.password_hash):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail='Incorrect email or password')
    token = create_access_token({'sub': user.id})
    return {'token': token, 'user': User.model_validate(user)}


@router.post('/register', status_code=201, response_model=RegisterResponse)
async def register(data: RegisterRequest, session: AsyncSession = Depends(get_session)):
    exists = await session.execute(select(models.User).where(models.User.email == data.email))
    if exists.scalars().first():
        raise HTTPException(status_code=400, detail='Email already registered')
    if data.role == models.UserRole.student and data.course is None:
        raise HTTPException(status_code=400, detail='Course is required for students')
    password_hash = get_password_hash(data.password)
    user = models.User(
        id=str(uuid.uuid4()),
        name=data.name,
        email=data.email,
        password_hash=password_hash,
        role=data.role,
        course=data.course
    )
    session.add(user)
    await session.commit()
    token = create_access_token({'sub': user.id})
    return {'token': token, 'user': User.model_validate(user)}

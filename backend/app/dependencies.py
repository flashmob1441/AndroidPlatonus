from typing import Type

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError
from sqlalchemy.ext.asyncio import AsyncSession

from app import models
from app.core.security import decode_token
from app.db.session import get_session
from app.models import User

oauth2_scheme = OAuth2PasswordBearer(tokenUrl='auth/login')


async def get_current_user(token: str = Depends(oauth2_scheme), session: AsyncSession = Depends(get_session)) -> Type[
    User]:
    credentials_exception = HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail='Could not validate credentials')
    try:
        payload = decode_token(token)
        user_id: str | None = payload.get('sub')
        if user_id is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    user = await session.get(models.User, user_id)
    if user is None:
        raise credentials_exception
    return user


async def get_current_teacher(user: models.User = Depends(get_current_user)) -> models.User:
    if user.role != models.UserRole.teacher:
        raise HTTPException(status_code=403, detail='Teacher access required')
    return user

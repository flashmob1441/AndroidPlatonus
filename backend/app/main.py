from contextlib import asynccontextmanager
from typing import AsyncGenerator

from fastapi import FastAPI
from app.routers import auth, schedule, grades, teacher
from app.db.session import engine, AsyncSessionLocal
from app import models
from app.seeds import seed


@asynccontextmanager
async def lifespan(_: FastAPI) -> AsyncGenerator[None, None]:
    async with engine.begin() as conn:
        await conn.run_sync(models.Base.metadata.create_all)
    async with AsyncSessionLocal() as session:
        await seed(session)
    yield

app = FastAPI(lifespan=lifespan)
app.include_router(auth.router)
app.include_router(schedule.router)
app.include_router(grades.router)
app.include_router(teacher.router)
import uuid
from datetime import datetime
from random import randint

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app import models
from app.core.security import get_password_hash


def _uid() -> str:
    return str(uuid.uuid4())


async def seed(session: AsyncSession) -> None:
    if (await session.execute(select(models.User).limit(1))).scalar():
        return

    teachers_info = [
        ("Mohsin F.A.", "mohsin@iitu.edu.kz"),
        ("Бердыкулова Г.М.", "berdykulova@iitu.edu.kz"),
        ("Желудков М.В.", "zheludkov@iitu.edu.kz"),
        ("Бекаулов Н.М.", "bekauov@iitu.edu.kz"),
        ("Аязбаев Г.М.", "ayazbaev@iitu.edu.kz"),
        ("Бекаулова Ж.М.", "bekaulova@iitu.edu.kz"),
        ("Қойшыбай С.С.", "koyshybay@iitu.edu.kz"),
        ("Ғазиз М.Н.", "gaziz@iitu.edu.kz"),
        ("Тоқанов О.С.", "tokanov@iitu.edu.kz"),
    ]
    teachers = {
        name: models.User(
            id=_uid(),
            name=name,
            email=email,
            password_hash=get_password_hash("password"),
            role=models.UserRole.teacher,
        )
        for name, email in teachers_info
    }

    group1 = models.Group(id=_uid(), name="IT2-2202")
    group2 = models.Group(id=_uid(), name="IT2-2203")

    student1 = models.User(
        id=_uid(),
        name="Студент Мурат А.А.",
        email="34444@iitu.edu.kz",
        password_hash=get_password_hash("44444444"),
        role=models.UserRole.student,
        course=3,
        group=group1,
    )
    student2 = models.User(
        id=_uid(),
        name="Студент Кто Т.О.",
        email="34445@iitu.edu.kz",
        password_hash=get_password_hash("55555555"),
        role=models.UserRole.student,
        course=3,
        group=group2,
    )

    period = models.AcademicPeriod(
        id=_uid(),
        year=2025,
        period=2,
        start_date=datetime(2025, 2, 1),
        end_date=datetime(2025, 6, 30),
    )

    subjects_map = {
        "Экономика и организация производства": "Бердыкулова Г.М.",
        "Операционные системы": "Mohsin F.A.",
        "Мобильные технологии и приложения (Android)": "Аязбаев Г.М.",
        "Разработка бизнес-компонентов и веб-сервисов (Java EE)": "Қойшыбай С.С.",
        "Front-end Разработка": "Желудков М.В.",
    }
    subjects = {n: models.Subject(id=_uid(), name=n) for n in subjects_map}

    lessons_template = [
        (1, "16:10 - 17:00", "online 15", "Л", "Операционные системы", "Mohsin F.A."),
        (1, "17:20 - 18:10", "online 17", "Л", "Операционные системы", "Mohsin F.A."),
        (2, "10:00 - 10:50", "Главный 605", "СПЗ", "Экономика и организация производства", "Бердыкулова Г.М."),
        (2, "11:00 - 11:50", "Главный 605", "СПЗ", "Экономика и организация производства", "Бердыкулова Г.М."),
        (2, "12:10 - 13:00", "Главный 702", "ЛЗ", "Front-end Разработка", "Желудков М.В."),
        (2, "13:10 - 14:00", "Главный 702", "ЛЗ", "Front-end Разработка", "Желудков М.В."),
        (3, "08:00 - 08:50", "Главный 901", "Л", "Мобильные технологии и приложения (Android)", "Аязбаев Г.М."),
        (3, "09:00 - 09:50", "Главный 901", "Л", "Мобильные технологии и приложения (Android)", "Аязбаев Г.М."),
        (3, "14:10 - 15:00", "Главный 406", "СПЗ", "Разработка бизнес-компонентов и веб-сервисов (Java EE)", "Қойшыбай С.С."),
        (6, "13:10 - 14:00", "Главный 800", "Л", "Разработка бизнес-компонентов и веб-сервисов (Java EE)", "Тоқанов О.С."),
    ]

    lessons2_template = [
        (1, "16:10 - 17:00", "online 15", "Л", "Операционные системы", "Mohsin F.A."),
        (1, "17:20 - 18:10", "online 17", "Л", "Операционные системы", "Mohsin F.A."),
        (2, "12:10 - 13:00", "Главный 601", "СПЗ", "Экономика и организация производства", "Бердыкулова Г.М."),
        (2, "13:10 - 14:00", "Главный 601", "СПЗ", "Экономика и организация производства", "Бердыкулова Г.М."),
        (2, "14:10 - 15:00", "Главный 703", "ЛЗ", "Front-end Разработка", "Желудков М.В."),
        (2, "15:10 - 16:00", "Главный 703", "ЛЗ", "Front-end Разработка", "Желудков М.В."),
        (3, "08:00 - 08:50", "Главный 901", "Л", "Мобильные технологии и приложения (Android)", "Аязбаев Г.М."),
        (3, "09:00 - 09:50", "Главный 901", "Л", "Мобильные технологии и приложения (Android)", "Аязбаев Г.М."),
        (3, "15:10 - 16:00", "Главный 401", "СПЗ", "Разработка бизнес-компонентов и веб-сервисов (Java EE)", "Қойшыбай С.С."),
        (6, "13:10 - 14:00", "Главный 800", "Л", "Разработка бизнес-компонентов и веб-сервисов (Java EE)", "Тоқанов О.С."),
    ]

    def _make_lessons(group_id: str, lessons_template: list):
        return [
            models.Lesson(
                id=_uid(),
                subject_id=subjects[subj].id,
                teacher_id=teachers[teacher].id,
                group_id=group_id,
                weekday=weekday,
                week_number=13,
                time=time,
                room=room,
                lesson_type=lesson_type,
            )
            for (weekday, time, room, lesson_type, subj, teacher) in lessons_template
        ]

    lessons = _make_lessons(group1.id, lessons_template) + _make_lessons(group2.id, lessons2_template)

    def _grades_for(student_id: str):
        return [
            models.Grade(
                id=_uid(),
                student_id=student_id,
                teacher_id=teachers[teacher_name].id,
                subject_id=subjects[subj_name].id,
                score=randint(70, 95),
                date=datetime.now(),
                academic_period_id=period.id,
                is_final=False,
            )
            for subj_name, teacher_name in subjects_map.items()
        ]

    grades = _grades_for(student1.id) + _grades_for(student2.id)

    session.add_all(
        [group1, group2]
        + list(teachers.values())
        + [student1, student2]
        + [period]
        + list(subjects.values())
        + lessons
        + grades
    )
    await session.commit()

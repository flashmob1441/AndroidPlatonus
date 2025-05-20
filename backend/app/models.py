import enum
from sqlalchemy import Column, String, Integer, DateTime, Boolean, Enum, ForeignKey, UniqueConstraint
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


class UserRole(str, enum.Enum):
    student = 'student'
    teacher = 'teacher'


class Group(Base):
    __tablename__ = 'groups'
    id = Column(String, primary_key=True)
    name = Column(String, unique=True, nullable=False)
    students = relationship('User', back_populates='group')
    lessons = relationship('Lesson', back_populates='group')


class User(Base):
    __tablename__ = 'users'
    id = Column(String, primary_key=True)
    name = Column(String, nullable=False)
    email = Column(String, unique=True, nullable=False)
    password_hash = Column(String, nullable=False)
    role = Column(Enum(UserRole), nullable=False)
    course = Column(Integer)
    group_id = Column(String, ForeignKey('groups.id'))
    group = relationship('Group', back_populates='students')
    grades = relationship('Grade', back_populates='student', foreign_keys='Grade.student_id')
    lessons = relationship('Lesson', back_populates='teacher', foreign_keys='Lesson.teacher_id')


class AcademicPeriod(Base):
    __tablename__ = 'academic_periods'
    id = Column(String, primary_key=True)
    year = Column(Integer, nullable=False)
    period = Column(Integer, nullable=False)
    start_date = Column(DateTime, nullable=False)
    end_date = Column(DateTime, nullable=False)
    __table_args__ = (UniqueConstraint('year', 'period', name='uix_year_period'),)


class Subject(Base):
    __tablename__ = 'subjects'
    id = Column(String, primary_key=True)
    name = Column(String, nullable=False)


class Lesson(Base):
    __tablename__ = 'lessons'
    id = Column(String, primary_key=True)
    subject_id = Column(String, ForeignKey('subjects.id'), nullable=False)
    teacher_id = Column(String, ForeignKey('users.id'), nullable=False)
    group_id = Column(String, ForeignKey('groups.id'), nullable=False)
    weekday = Column(Integer, nullable=False)
    week_number = Column(Integer)
    time = Column(String, nullable=False)
    room = Column(String)
    lesson_type = Column(String)
    subject = relationship('Subject')
    teacher = relationship('User', back_populates='lessons')
    group = relationship('Group', back_populates='lessons')


class Grade(Base):
    __tablename__ = 'grades'
    id = Column(String, primary_key=True)
    student_id = Column(String, ForeignKey('users.id'), nullable=False)
    teacher_id = Column(String, ForeignKey('users.id'), nullable=False)
    subject_id = Column(String, ForeignKey('subjects.id'), nullable=False)
    score = Column(Integer, nullable=False)
    date = Column(DateTime, nullable=False)
    academic_period_id = Column(String, ForeignKey('academic_periods.id'), nullable=False)
    is_final = Column(Boolean, default=False)
    student = relationship('User', foreign_keys=[student_id], back_populates='grades')
    teacher = relationship('User', foreign_keys=[teacher_id])
    subject = relationship('Subject')
    period = relationship('AcademicPeriod')

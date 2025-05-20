from datetime import datetime
from typing import Dict, List, Optional
from enum import Enum
from pydantic import BaseModel, EmailStr, ConfigDict


class UserRole(str, Enum):
    student = 'student'
    teacher = 'teacher'


class User(BaseModel):
    id: str
    name: str
    email: EmailStr
    role: UserRole
    course: Optional[int]
    groupId: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)


class Token(BaseModel):
    token: str
    user: User


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class Subject(BaseModel):
    id: str
    name: str
    teacherId: str
    teacherName: str
    time: str
    room: str
    lessonType: str
    dayOfWeek: int
    weekNumber: Optional[int]


class ScheduleResponse(BaseModel):
    days: Dict[int, List[Subject]]


class GradeDto(BaseModel):
    id: str
    studentId: str
    subjectId: str
    subjectName: str
    teacherName: str
    score: int
    date: datetime
    academicPeriodId: str
    isFinal: bool


class GradesResponse(BaseModel):
    grades: List[GradeDto]


class GradeHistoryResponse(BaseModel):
    history: List[GradeDto]


class StudentsResponse(BaseModel):
    students: List[User]


class Group(BaseModel):
    id: str
    name: str

    model_config = ConfigDict(from_attributes=True)


class GroupsResponse(BaseModel):
    groups: List[Group]


class SubmitGradeRequest(BaseModel):
    teacherId: str
    studentId: str
    subjectId: str
    score: int
    date: int


class SubmitGradeResponse(BaseModel):
    success: bool


class RegisterRequest(BaseModel):
    name: str
    email: EmailStr
    password: str
    role: UserRole
    course: Optional[int] = None


class RegisterResponse(BaseModel):
    token: str
    user: User
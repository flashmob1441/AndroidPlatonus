import os


class Settings:
    secret_key: str = os.getenv('SECRET_KEY', 'change_me')
    algorithm: str = 'HS256'
    access_token_expire_minutes: int = 60 * 24 * 7
    database_url: str = os.getenv('DATABASE_URL', 'postgresql+asyncpg://platonus:platonus@localhost:5432/platonus')


settings = Settings()
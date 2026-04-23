from typing import Generator

from sqlalchemy import create_engine, text
from sqlalchemy.orm import declarative_base, sessionmaker, Session


# Example:
# DATABASE_URL = "postgresql+psycopg2://user:password@localhost:5432/student_mental_health"
# DATABASE_URL = "postgresql://postgres:Yug%40112007@localhost:5432/student_mental_health"
DATABASE_URL = "postgresql://postgres:yug112007@stdmentalhealthojt.czc2g2m46gl0.ap-south-1.rds.amazonaws.com:5432/postgres"

engine = create_engine(DATABASE_URL, echo=False, future=True)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine, class_=Session)

Base = declarative_base()


def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


def run_startup_migrations() -> None:
    """
    Add columns introduced after the table was first created.
    SQLAlchemy create_all() does not ALTER existing tables.
    """
    if not engine.url.drivername.startswith("postgresql"):
        return

    alters = [
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS anxiety_score DOUBLE PRECISION",
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS stress_score DOUBLE PRECISION",
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS depression_score DOUBLE PRECISION",
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS anxiety_label VARCHAR(100)",
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS stress_label VARCHAR(100)",
        "ALTER TABLE student_indicator_entries ADD COLUMN IF NOT EXISTS depression_label VARCHAR(100)",
    ]

    with engine.begin() as conn:
        for stmt in alters:
            conn.execute(text(stmt))


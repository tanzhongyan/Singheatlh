#!/usr/bin/env python3
"""
Mock Data Generator for SingHealth Clinic System
Generates relationally-consistent CSV files based on existing clinic data
"""

import csv
import random
import uuid
from datetime import datetime, timedelta
from typing import List, Dict, Tuple

# Configuration
OUTPUT_DIR = "db/sample-data"
TODAY = datetime(2025, 10, 29)
SCHEDULE_END_DATE = datetime(2025, 11, 15)  # Schedules extend to Nov 15
NUM_PATIENTS = 800
NUM_APPOINTMENTS = 5000
APPOINTMENT_DURATION_MINUTES = 15

# Patient arrival scenarios for today's appointments
ARRIVAL_SCENARIOS = {
    "early": 0.35,      # 35% arrive 5-30 min early
    "on_time": 0.30,    # 30% arrive 0-5 min early
    "late": 0.20,       # 20% arrive 5-20 min late
    "very_late": 0.10,  # 10% arrive 20-40 min late
    "no_show": 0.05     # 5% don't show up at all
}

# Sample data for generating realistic names
FIRST_NAMES = [
    "Wei", "Ming", "Hui", "Xin", "Jun", "Li", "Chen", "Yan", "Jing", "Rui",
    "Sarah", "Emily", "David", "Michael", "Jessica", "James", "Linda", "Robert",
    "Mary", "John", "Tan", "Lim", "Wong", "Ng", "Lee", "Goh", "Ong", "Teo",
    "Rachel", "Kevin", "Grace", "Steven", "Nicole", "Benjamin", "Amanda", "Daniel",
    "Michelle", "Andrew", "Jennifer", "Marcus", "Rajesh", "Kumar", "Priya"
]

LAST_NAMES = [
    "Tan", "Lim", "Wong", "Ng", "Lee", "Goh", "Ong", "Teo", "Koh", "Sim",
    "Chen", "Yeo", "Low", "Chua", "Tay", "Ho", "Yap", "Ang", "Chng", "Soh",
    "Kumar", "Singh", "Krishnan", "Nair", "Rahman", "Hassan", "Ali", "Chong"
]

TREATMENT_SUMMARIES = [
    "Patient presented with flu-like symptoms. Prescribed rest and medication.",
    "Routine health checkup completed. All vitals normal.",
    "Patient complained of persistent cough. Prescribed antibiotics and cough syrup.",
    "Follow-up consultation for chronic condition. Medication adjusted.",
    "Minor injury treated and bandaged. Advised to return if condition worsens.",
    "Vaccination administered. Patient advised on possible side effects.",
    "Blood test results reviewed. All parameters within normal range.",
    "Patient presented with headache and fever. Prescribed pain relief medication.",
    "Skin condition examined. Topical cream prescribed.",
    "Consultation for back pain. Referred to physiotherapy.",
]


def generate_name() -> str:
    """Generate a random Singaporean name"""
    return f"{random.choice(FIRST_NAMES)} {random.choice(LAST_NAMES)}"


def generate_email(name: str, suffix: str = "example.com") -> str:
    """Generate email from name"""
    clean_name = name.lower().replace(" ", ".")
    return f"{clean_name}@{suffix}"


def generate_phone() -> str:
    """Generate Singapore phone number"""
    return f"+659{random.randint(1000000, 9999999)}"


def parse_time(time_str: str) -> datetime.time:
    """Parse time string like '8:00:00 am' to time object"""
    time_str = time_str.strip()
    try:
        return datetime.strptime(time_str, "%I:%M:%S %p").time()
    except:
        try:
            return datetime.strptime(time_str, "%H:%M:%S").time()
        except:
            return datetime.strptime("09:00:00", "%H:%M:%S").time()


def load_clinics() -> List[Dict]:
    """Load existing clinic data"""
    clinics = []
    with open(f"{OUTPUT_DIR}/clinics.csv", "r", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for idx, row in enumerate(reader, start=1):
            clinics.append({
                "clinic_id": idx,
                "name": row["name"],
                "type": row["type"],
                "opening_hours": parse_time(row["opening_hours"]),
                "closing_hours": parse_time(row["closing_hours"])
            })
    return clinics


def generate_user_profiles(num_clinics: int) -> List[Dict]:
    """Generate user_profile.csv data"""
    users = []

    # 1. System Admin
    admin_id = str(uuid.uuid4())
    users.append({
        "user_id": admin_id,
        "name": "System Admin",
        "role": "S",
        "email": "admin@system.com",
        "telephone_number": "+6512345678",
        "clinic_id": ""
    })

    # 2. Clinic Staff (1-2 per clinic)
    # Generate all possible unique name permutations
    name_permutations = [f"{fn} {ln}" for fn in FIRST_NAMES for ln in LAST_NAMES]
    random.shuffle(name_permutations)

    users = []
    used_emails = set(["admin@system.com"])
    name_idx = 0

    # 1. System Admin
    admin_id = str(uuid.uuid4())
    users.append({
        "user_id": admin_id,
        "name": "System Admin",
        "role": "S",
        "email": "admin@system.com",
        "telephone_number": "+6512345678",
        "clinic_id": ""
    })

    email_counter = 1

    # 2. Clinic Staff (1-2 per clinic)
    for clinic_id in range(1, num_clinics + 1):
        num_staff = random.randint(1, 2)
        for _ in range(num_staff):
            name = name_permutations[name_idx % len(name_permutations)]
            name_idx += 1
            email = f"{name.lower().replace(' ', '.')}+{email_counter}@example.com"
            email_counter += 1
            users.append({
                "user_id": str(uuid.uuid4()),
                "name": name,
                "role": "C",
                "email": email,
                "telephone_number": generate_phone(),
                "clinic_id": str(clinic_id)
            })

    # 3. Patients
    for _ in range(NUM_PATIENTS):
        name = name_permutations[name_idx % len(name_permutations)]
        name_idx += 1
        email = f"{name.lower().replace(' ', '.')}+{email_counter}@example.com"
        email_counter += 1
        users.append({
            "user_id": str(uuid.uuid4()),
            "name": name,
            "role": "P",
            "email": email,
            "telephone_number": generate_phone(),
            "clinic_id": ""
        })

    return users


def generate_doctors(num_clinics: int) -> List[Dict]:
    """Generate doctor.csv data (2-5 doctors per clinic)"""
    doctors = []
    doctor_counter = 1

    for clinic_id in range(1, num_clinics + 1):
        num_doctors = random.randint(2, 5)
        for _ in range(num_doctors):
            name = f"Dr. {generate_name()}"
            # Appointment duration: 15, 20, or 30 minutes (most doctors use 15)
            appointment_duration = random.choices([15, 20, 30], weights=[0.7, 0.2, 0.1])[0]
            doctors.append({
                "doctor_id": f"D{str(doctor_counter).zfill(9)}",
                "name": name,
                "clinic_id": str(clinic_id),
                "appointment_duration_in_minutes": str(appointment_duration)
            })
            doctor_counter += 1

    return doctors


def generate_schedules(doctors: List[Dict], clinics: List[Dict]) -> List[Dict]:
    """Generate schedule.csv data from past 7 days to November 15"""
    schedules = []
    schedule_counter = 1

    # Create clinic lookup
    clinic_lookup = {c["clinic_id"]: c for c in clinics}

    # Calculate total days from 7 days ago to Nov 15 (inclusive)
    start_date = TODAY - timedelta(days=7)
    total_days = (SCHEDULE_END_DATE - start_date).days + 1  # +1 to include end date

    for doctor in doctors:
        clinic_id = int(doctor["clinic_id"])
        clinic = clinic_lookup[clinic_id]

        # Generate schedules from start_date to SCHEDULE_END_DATE
        for day_offset in range(total_days):
            schedule_date = start_date + timedelta(days=day_offset)

            # Skip weekends randomly (30% chance)
            if schedule_date.weekday() >= 5 and random.random() < 0.3:
                continue

            opening = datetime.combine(schedule_date, clinic["opening_hours"])
            closing = datetime.combine(schedule_date, clinic["closing_hours"])

            # Create schedule blocks for the day
            current_time = opening

            while current_time < closing:
                # Randomly create AVAILABLE or UNAVAILABLE blocks
                is_lunch = (current_time.hour == 12 or current_time.hour == 13) and random.random() < 0.7

                if is_lunch:
                    block_type = "UNAVAILABLE"
                    duration_hours = random.choice([1, 1.5])
                else:
                    block_type = "AVAILABLE" if random.random() < 0.85 else "UNAVAILABLE"
                    duration_hours = random.choice([2, 3, 4])

                end_time = current_time + timedelta(hours=duration_hours)

                # Don't exceed closing time
                if end_time > closing:
                    end_time = closing

                schedules.append({
                    "schedule_id": f"S{str(schedule_counter).zfill(9)}",
                    "doctor_id": doctor["doctor_id"],
                    "start_datetime": current_time.strftime("%Y-%m-%d %H:%M:%S"),
                    "end_datetime": end_time.strftime("%Y-%m-%d %H:%M:%S"),
                    "type": block_type
                })

                schedule_counter += 1
                current_time = end_time

    return schedules


def generate_appointments(
    schedules: List[Dict],
    patients: List[str],
    doctors: List[Dict]
) -> List[Dict]:
    """Generate appointment.csv data (5000+ appointments in 15-min slots)"""
    appointments = []
    appointment_counter = 1

    # Filter only AVAILABLE schedules
    available_schedules = [s for s in schedules if s["type"] == "AVAILABLE"]

    # Track doctor time slots to avoid overlaps
    doctor_slots = {}  # doctor_id -> set of (start_datetime)

    # Build doctor lookup
    doctor_lookup = {d["doctor_id"]: d for d in doctors}

    # Generate appointments
    attempts = 0
    max_attempts = NUM_APPOINTMENTS * 3

    while len(appointments) < NUM_APPOINTMENTS and attempts < max_attempts:
        attempts += 1

        # Pick a random available schedule
        schedule = random.choice(available_schedules)
        doctor_id = schedule["doctor_id"]

        # Parse schedule times
        schedule_start = datetime.strptime(schedule["start_datetime"], "%Y-%m-%d %H:%M:%S")
        schedule_end = datetime.strptime(schedule["end_datetime"], "%Y-%m-%d %H:%M:%S")

        # Calculate total 15-min slots in this schedule
        total_minutes = int((schedule_end - schedule_start).total_seconds() / 60)
        num_slots = total_minutes // APPOINTMENT_DURATION_MINUTES

        if num_slots == 0:
            continue

        # Pick a random slot
        slot_index = random.randint(0, num_slots - 1)
        appt_start = schedule_start + timedelta(minutes=slot_index * APPOINTMENT_DURATION_MINUTES)
        appt_end = appt_start + timedelta(minutes=APPOINTMENT_DURATION_MINUTES)

        # Check for overlaps
        if doctor_id not in doctor_slots:
            doctor_slots[doctor_id] = set()

        appt_start_str = appt_start.strftime("%Y-%m-%d %H:%M:%S")

        if appt_start_str in doctor_slots[doctor_id]:
            continue  # Overlap detected, skip

        # Determine status based on date
        if appt_start.date() < TODAY.date():
            status = "Completed" if random.random() < 0.9 else "Cancelled"
        elif appt_start.date() == TODAY.date():
            status = "Upcoming"
        else:
            status = "Upcoming" if random.random() < 0.95 else "Cancelled"

        # Create appointment
        appointments.append({
            "appointment_id": f"A{str(appointment_counter).zfill(9)}",
            "patient_id": random.choice(patients),
            "doctor_id": doctor_id,
            "start_datetime": appt_start_str,
            "end_datetime": appt_end.strftime("%Y-%m-%d %H:%M:%S"),
            "status": status
        })

        doctor_slots[doctor_id].add(appt_start_str)
        appointment_counter += 1

    return appointments


def generate_medical_summaries(appointments: List[Dict]) -> List[Dict]:
    """Generate medical_summary.csv for completed appointments"""
    summaries = []
    summary_counter = 1

    for appt in appointments:
        if appt["status"] == "Completed":
            summaries.append({
                "summary_id": f"M{str(summary_counter).zfill(9)}",
                "appointment_id": appt["appointment_id"],
                "treatment_summary": random.choice(TREATMENT_SUMMARIES)
            })
            summary_counter += 1

    return summaries


def get_arrival_scenario() -> str:
    """Randomly select an arrival scenario based on probabilities"""
    rand = random.random()
    cumulative = 0
    for scenario, probability in ARRIVAL_SCENARIOS.items():
        cumulative += probability
        if rand <= cumulative:
            return scenario
    return "on_time"


def generate_queue_tickets(appointments: List[Dict]) -> Tuple[List[Dict], List[str]]:
    """
    Generate queue_ticket.csv for today's upcoming appointments with realistic arrival scenarios.
    Returns: (tickets, no_show_appointment_ids)
    """
    tickets = []
    no_show_appointment_ids = []
    ticket_counter = 1
    queue_number = 1001

    # Filter appointments for today with Upcoming status
    today_appointments = [
        a for a in appointments
        if a["status"] == "Upcoming" and a["start_datetime"].startswith(TODAY.strftime("%Y-%m-%d"))
    ]

    # Sort by start time for logical queue ordering
    today_appointments.sort(key=lambda x: x["start_datetime"])

    for appt in today_appointments:
        appt_time = datetime.strptime(appt["start_datetime"], "%Y-%m-%d %H:%M:%S")
        scenario = get_arrival_scenario()

        if scenario == "no_show":
            # Patient didn't show up - no queue ticket created
            no_show_appointment_ids.append(appt["appointment_id"])
            continue

        # Determine check-in time based on scenario
        if scenario == "early":
            # Arrive 5-30 minutes early
            minutes_offset = -random.randint(5, 30)
        elif scenario == "on_time":
            # Arrive 0-5 minutes early
            minutes_offset = -random.randint(0, 5)
        elif scenario == "late":
            # Arrive 5-20 minutes late
            minutes_offset = random.randint(5, 20)
        elif scenario == "very_late":
            # Arrive 20-40 minutes late
            minutes_offset = random.randint(20, 40)
        else:
            minutes_offset = -10  # default

        checkin_time = appt_time + timedelta(minutes=minutes_offset)

        # Determine queue status based on current time and appointment time
        # Assuming current time is around 2pm on TODAY
        current_time = TODAY.replace(hour=14, minute=0, second=0)

        if appt_time < current_time:
            # Past appointment on today - should be in various stages
            if checkin_time < current_time - timedelta(hours=1):
                status = "COMPLETED"
            elif checkin_time < current_time - timedelta(minutes=30):
                status = "IN_CONSULTATION"
            else:
                status = "CALLED"
        else:
            # Future appointment today - still waiting
            status = "CHECKED_IN"

        tickets.append({
            "ticket_id": str(ticket_counter),
            "appointment_id": appt["appointment_id"],
            "status": status,
            "check_in_time": checkin_time.strftime("%Y-%m-%d %H:%M:%S"),
            "queue_number": str(queue_number),
            "is_fast_tracked": "FALSE",
            "fast_track_reason": ""
        })

        ticket_counter += 1
        queue_number += 1

    return tickets, no_show_appointment_ids


def write_csv(filename: str, data: List[Dict], fieldnames: List[str]):
    """Write data to CSV file"""
    filepath = f"{OUTPUT_DIR}/{filename}"
    with open(filepath, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(data)
    print(f"[OK] Generated {filepath} ({len(data)} records)")


def main():
    print("Starting mock data generation...")
    print(f"Today's date: {TODAY.strftime('%Y-%m-%d')}\n")

    # Load existing clinics
    print("Loading existing clinic data...")
    clinics = load_clinics()
    num_clinics = len(clinics)
    print(f"[OK] Loaded {num_clinics} clinics\n")

    # Generate data in order
    print("Generating user_profile.csv...")
    users = generate_user_profiles(num_clinics)
    patients = [u["user_id"] for u in users if u["role"] == "P"]
    write_csv("user_profile.csv", users,
              ["user_id", "name", "role", "email", "telephone_number", "clinic_id"])

    print("\nGenerating doctor.csv...")
    doctors = generate_doctors(num_clinics)
    write_csv("doctor.csv", doctors, ["doctor_id", "name", "clinic_id", "appointment_duration_in_minutes"])

    print("\nGenerating schedule.csv...")
    schedules = generate_schedules(doctors, clinics)
    write_csv("schedule.csv", schedules,
              ["schedule_id", "doctor_id", "start_datetime", "end_datetime", "type"])

    print("\nGenerating appointment.csv...")
    appointments = generate_appointments(schedules, patients, doctors)
    write_csv("appointment.csv", appointments,
              ["appointment_id", "patient_id", "doctor_id", "start_datetime", "end_datetime", "status"])

    print("\nGenerating medical_summary.csv...")
    summaries = generate_medical_summaries(appointments)
    write_csv("medical_summary.csv", summaries,
              ["summary_id", "appointment_id", "treatment_summary"])

    print("\nGenerating queue_ticket.csv...")
    tickets, no_show_ids = generate_queue_tickets(appointments)

    # Update appointments that were no-shows
    if no_show_ids:
        print(f"  Marking {len(no_show_ids)} appointments as 'Missed' (no-shows)")
        for appt in appointments:
            if appt["appointment_id"] in no_show_ids:
                appt["status"] = "Missed"

        # Re-write appointments.csv with updated statuses
        write_csv("appointment.csv", appointments,
                  ["appointment_id", "patient_id", "doctor_id", "start_datetime", "end_datetime", "status"])

    # Only output columns matching the table
    queue_ticket_table_fields = ["appointment_id", "status", "check_in_time", "queue_number", "is_fast_tracked"]
    queue_ticket_table_data = [
        {k: t[k] for k in queue_ticket_table_fields} for t in tickets
    ]
    write_csv("queue_ticket.csv", queue_ticket_table_data, queue_ticket_table_fields)

    # Calculate statistics
    ticket_status_counts = {}
    for ticket in tickets:
        status = ticket["status"]
        ticket_status_counts[status] = ticket_status_counts.get(status, 0) + 1

    print("\n" + "="*60)
    print("Mock data generation complete!")
    print("="*60)
    print(f"Summary:")
    print(f"  - Clinics: {num_clinics} (existing)")
    print(f"  - Users: {len(users)} (1 admin, {len([u for u in users if u['role']=='C'])} staff, {len(patients)} patients)")
    print(f"  - Doctors: {len(doctors)}")
    print(f"  - Schedules: {len(schedules)} (through {SCHEDULE_END_DATE.strftime('%Y-%m-%d')})")
    print(f"  - Appointments: {len(appointments)}")
    print(f"  - Medical Summaries: {len(summaries)}")
    print(f"  - Queue Tickets: {len(tickets)} (for today)")
    if ticket_status_counts:
        print(f"    Queue Status Distribution:")
        for status, count in sorted(ticket_status_counts.items()):
            print(f"      - {status}: {count}")
    print(f"  - No-shows: {len(no_show_ids)} patients didn't check in")


if __name__ == "__main__":
    main()
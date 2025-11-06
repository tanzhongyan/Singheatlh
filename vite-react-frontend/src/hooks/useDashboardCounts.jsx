import { useState, useEffect } from "react";
import apiClient from "../api/apiClient";
export const useDashboardCounts = () => {
  const [userCount, setUserCount] = useState(0);
  const [doctorCount, setDoctorCount] = useState(0);
  const [clinicCount, setClinicCount] = useState(0);

  useEffect(() => {
    const fetchCounts = async () => {
      try {
        const userResponse = await apiClient.get("/api/system-administrators/users/count");
        const clinicResponse = await apiClient.get("/api/system-administrators/clinics/count");
        const doctorResponse = await apiClient.get("/api/system-administrators/doctors/count");
        setUserCount(userResponse.data);
        setClinicCount(clinicResponse.data);
        setDoctorCount(doctorResponse.data);
      } catch (error) {
        console.error("Error fetching counts:", error);
      }
    };

    fetchCounts();
  }, []);

  return { userCount, doctorCount, clinicCount };
};

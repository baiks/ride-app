// app/actions/users.ts
"use server";

import  {apiRequest}  from '../lib/api';


export async function updateDriverStatusAction(
  driverId: number,
  status: string
) {
  try {
    await apiRequest(`/users/drivers/${driverId}/status?status=${status}`, {
      method: "PUT",
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function updateUserStatusAction(userId: number, status: boolean) {
  try {
    const user = await apiRequest(
      `/users/user/${userId}/status?status=${status}`,
      {
        method: "PUT",
      }
    );
    return { success: true, data: user };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function getAllDrivers() {
  try {
    return await apiRequest("/users/drivers");
  } catch (error) {
    return [];
  }
}

export async function getAllUsers() {
  try {
    return await apiRequest("/users");
  } catch (error) {
    return [];
  }
}
export async function updateUserAction(userId: number, formData: any) {
  try {
    const user = await apiRequest(
      `/users/${userId}`,
      {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(formData),
      }
    );
    return { success: true, data: user };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

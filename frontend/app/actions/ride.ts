// app/actions/rides.ts
"use server";

import { apiRequest } from "../lib/api";

export async function requestRideAction(customerId: number, data: any) {
  try {
    await apiRequest(`/rides/request?customerId=${customerId}`, {
      method: "POST",
      body: JSON.stringify(data),
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function acceptRideAction(rideId: number, driverId: number) {
  try {
    await apiRequest(`/rides/${rideId}/accept?driverId=${driverId}`, {
      method: "PUT",
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function startRideAction(rideId: number) {
  try {
    await apiRequest(`/rides/${rideId}/start`, {
      method: "PUT",
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function completeRideAction(rideId: number) {
  try {
    await apiRequest(`/rides/${rideId}/complete`, {
      method: "PUT",
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function cancelRideAction(
  rideId: number,
  userId: number,
  reason?: string
) {
  try {
    const url = `/rides/${rideId}/cancel?userId=${userId}${
      reason ? `&reason=${encodeURIComponent(reason)}` : ""
    }`;
    await apiRequest(url, {
      method: "PUT",
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}

export async function getCustomerRides(customerId: number) {
  try {
    return await apiRequest(`/rides/customer/${customerId}`);
  } catch (error) {
    return [];
  }
}

export async function getDriverRides(driverId: number) {
  try {
    return await apiRequest(`/rides/driver/${driverId}`);
  } catch (error) {
    return [];
  }
}

export async function getAllRides() {
  try {
    return await apiRequest(`/rides`);
  } catch (error) {
    return [];
  }
}

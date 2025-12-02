// app/actions/location.ts
'use server'

import  {apiRequest}  from '../lib/api';


export async function updateLocationAction(driverId: number, data: any) {
  try {
    await apiRequest(`/location/update?driverId=${driverId}`, {
      method: 'POST',
      body: JSON.stringify(data),
    });
    return { success: true };
  } catch (error) {
    return { success: false, error: (error as Error).message };
  }
}
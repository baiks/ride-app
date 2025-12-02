"use server";

import { cookies } from "next/headers";
import { apiRequest, handleApiError } from "../lib/api";

export async function loginAction(email: string, password: string) {
  try {
    const response = await apiRequest("/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });

    const cookieStore = await cookies();
    cookieStore.set("token", response.token, {
      httpOnly: true,
      secure: process.env.APP_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 7, // 1 week
    });

    cookieStore.set("user", JSON.stringify(response.user), {
      httpOnly: true,
      secure: process.env.APP_ENV === "production",
      sameSite: "lax",
      maxAge: 60 * 60 * 24 * 7,
    });

    return { success: true, user: response.user };
  } catch (error) {
    return handleApiError(error);
  }
}

export async function registerAction(data: any) {
  try {
    await apiRequest("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });

    return { success: true };
  } catch (error) {
    console.error(error);
    return handleApiError(error);
  }
}

export async function logoutAction() {
  const cookieStore = await cookies();
  cookieStore.delete("token");
  cookieStore.delete("user");
  return { success: true };
}

export async function getCurrentUser() {
  const cookieStore = await cookies();
  const userCookie = cookieStore.get("user")?.value;

  if (!userCookie) {
    return null;
  }

  try {
    return JSON.parse(userCookie);
  } catch {
    return null;
  }
}

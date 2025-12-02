import { cookies } from "next/headers";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api";

const REQUEST_TIMEOUT = 30000; // 30 seconds

// Helper function to format validation errors
function formatValidationErrors(details: Record<string, string>): string {
  if (!details || typeof details !== "object") return "";

  const errors = Object.entries(details).map(([field, message]) => {
    const fieldName =
      field.charAt(0).toUpperCase() + field.slice(1).replace(/([A-Z])/g, " $1");
    return `• ${fieldName}: ${message}`;
  });

  return errors.join("\n");
}

export async function apiRequest(endpoint: string, options: RequestInit = {}) {
  console.log("End Point:", `${API_BASE_URL}${endpoint}`);
  console.log("Env:", `${process.env.APP_ENV}`);
  try {
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    const headers = {
      "Content-Type": "application/json",
      ...(token && { Authorization: `Bearer ${token}` }),
      ...options.headers,
    };

    // Create abort controller for timeout
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT);

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
      signal: controller.signal,
    });

    clearTimeout(timeoutId);

    console.log("Response status:", response.status);

    // Clone the response so we can read it multiple times
    const responseClone = response.clone();

    // Try to parse the response body
    let responseData;
    try {
      responseData = await responseClone.json();
      console.log("Response data:", responseData);
    } catch (parseError) {
      // Response body is not JSON or empty
      responseData = null;
    }

    if (!response.ok) {
      // If we have error details from the backend, use them
      if (responseData) {
        const error: any = new Error(
          responseData.message || responseData.error || response.statusText
        );
        error.status = responseData.status || response.status;
        error.details = responseData.details; // Validation errors
        error.fullResponse = responseData;
        throw error;
      }

      // Fallback error if no body
      throw new Error(`API Error: ${response.status} ${response.statusText}`);
    }

    // Return the parsed data for successful responses
    return responseData;
  } catch (error: any) {
    console.error("API Request failed:", endpoint, error);

    // Handle timeout
    if (error.name === "AbortError") {
      const timeoutError: any = new Error(
        "Request timeout. Please check your connection and try again."
      );
      timeoutError.status = 408;
      timeoutError.isTimeout = true;
      throw timeoutError;
    }

    // Handle network errors
    if (error instanceof TypeError && error.message === "Failed to fetch") {
      const networkError: any = new Error(
        "Network error. Please check your internet connection."
      );
      networkError.status = 0;
      networkError.isNetworkError = true;
      throw networkError;
    }

    throw error;
  }
}

// Generic error handler that processes all error types
export function handleApiError(error: any) {
  console.error("API error:", error);

  // Handle 401 Unauthorized (already handled in apiRequest, but for safety)
  if (error.status === 401) {
    return {
      success: false,
      error: "Invalid credentials or Session expired. Please login again.",
      status: 401,
      shouldLogout: true,
    };
  }

  // Handle timeout
  if (error.isTimeout) {
    return {
      success: false,
      error: "Request timed out. Please check your connection and try again.",
      status: 408,
    };
  }

  // Handle network errors
  if (error.isNetworkError) {
    return {
      success: false,
      error: "Network error. Please check your internet connection.",
      status: 0,
    };
  }

  // Handle validation errors with details
  if (error.details && typeof error.details === "object") {
    const validationMessage = formatValidationErrors(error.details);
    return {
      success: false,
      error: validationMessage || error.message || "Validation failed",
      validationErrors: error.details,
      status: error.status || 400,
    };
  }

  // Generic handler for all HTTP errors with standard structure
  return {
    success: false,
    error: error.message || "An error occurred. Please try again.",
    status: error.status || 500,
  };
}

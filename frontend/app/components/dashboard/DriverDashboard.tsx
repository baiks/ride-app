"use client";

import { useEffect, useState, useMemo } from "react";
import { useRouter } from "next/navigation";
import toast, { Toaster } from "react-hot-toast";
import { MapPin, Navigation } from "lucide-react";
import {
  acceptRideAction,
  startRideAction,
  completeRideAction,
  cancelRideAction,
} from "../../actions/ride";
import { updateDriverStatusAction } from "../../actions/user";
import { updateLocationAction } from "../../actions/location";
import RideCard from "../RiderCard";

interface DriverDashboardProps {
  user: any;
  rides: any[];
}

export default function DriverDashboard({ user, rides }: DriverDashboardProps) {
  const router = useRouter();
  const [status, setStatus] = useState(user.driverStatus || "OFFLINE");
  const [cancellingRide, setCancellingRide] = useState<number | null>(null);
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [selectedRideId, setSelectedRideId] = useState<number | null>(null);
  const [cancellationReason, setCancellationReason] = useState("");
  const [showLocationModal, setShowLocationModal] = useState(false);
  const [currentLocation, setCurrentLocation] = useState({
    latitude: -1.2921,
    longitude: 36.8219,
    address: "Nairobi CBD",
  });
  const [mapLoaded, setMapLoaded] = useState(false);
  const [mapRef, setMapRef] = useState<any>(null);
  const [markerRef, setMarkerRef] = useState<any>(null);

  // Sort rides by most recent first
  const sortedRides = useMemo(() => {
    return [...rides].sort((a, b) => {
      const dateA = new Date(a.createdAt || a.requestedAt || 0).getTime();
      const dateB = new Date(b.createdAt || b.requestedAt || 0).getTime();
      return dateB - dateA;
    });
  }, [rides]);

  // Sync status with user prop when it changes
  useEffect(() => {
    setStatus(user.driverStatus || "OFFLINE");
  }, [user.driverStatus]);

  useEffect(() => {
    if (showLocationModal && typeof window !== "undefined") {
      loadLeaflet();
    }
  }, [showLocationModal]);

  const loadLeaflet = () => {
    if (!document.querySelector('link[href*="leaflet.css"]')) {
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      document.head.appendChild(link);
    }

    if (!(window as any).L) {
      const script = document.createElement("script");
      script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      script.onload = () => {
        setMapLoaded(true);
        setTimeout(initializeMap, 100);
      };
      document.head.appendChild(script);
    } else {
      setMapLoaded(true);
      setTimeout(initializeMap, 100);
    }
  };

  const initializeMap = () => {
    const container = document.getElementById("location-map");
    if (!container || !(window as any).L || mapRef) return;

    const L = (window as any).L;
    const map = L.map(container).setView(
      [currentLocation.latitude, currentLocation.longitude],
      13
    );

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
    }).addTo(map);

    const icon = L.divIcon({
      html: `<div style="background-color: #3b82f6; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });

    const marker = L.marker(
      [currentLocation.latitude, currentLocation.longitude],
      { icon, draggable: true }
    ).addTo(map);

    marker.on("dragend", (e: any) => {
      const pos = e.target.getLatLng();
      updateLocationCoords(pos.lat, pos.lng);
    });

    map.on("click", (e: any) => {
      const { lat, lng } = e.latlng;
      marker.setLatLng([lat, lng]);
      updateLocationCoords(lat, lng);
    });

    setMapRef(map);
    setMarkerRef(marker);
  };

  const updateLocationCoords = async (lat: number, lng: number) => {
    setCurrentLocation((prev) => ({
      ...prev,
      latitude: lat,
      longitude: lng,
      address: "Loading address...",
    }));

    const address = await reverseGeocode(lat, lng);
    setCurrentLocation((prev) => ({
      ...prev,
      address,
    }));
  };

  const reverseGeocode = async (lat: number, lng: number): Promise<string> => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18`,
        { headers: { "User-Agent": "RideSharingApp/1.0" } }
      );
      const data = await response.json();
      return data.display_name || `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    } catch (error) {
      return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
    }
  };

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      toast.error("Geolocation is not supported by your browser");
      return;
    }

    const loadingToast = toast.loading("Getting your location...");
    navigator.geolocation.getCurrentPosition(
      async (position) => {
        const lat = position.coords.latitude;
        const lng = position.coords.longitude;

        setCurrentLocation({
          latitude: lat,
          longitude: lng,
          address: "Loading address...",
        });

        markerRef?.setLatLng([lat, lng]);
        mapRef?.panTo([lat, lng]);

        const address = await reverseGeocode(lat, lng);
        setCurrentLocation((prev) => ({ ...prev, address }));

        toast.success("Location updated!", { id: loadingToast });
      },
      (error) => {
        toast.error("Unable to get your location", { id: loadingToast });
      }
    );
  };

  const handleUpdateLocation = async () => {
    const loadingToast = toast.loading("Updating location...");

    const result = await updateLocationAction(user.id, {
      latitude: currentLocation.latitude,
      longitude: currentLocation.longitude,
    });

    toast.dismiss(loadingToast);

    if (result.success) {
      toast.success("Location updated successfully!");
      setShowLocationModal(false);
      router.refresh();
    } else {
      toast.error("Failed to update location: " + result.error);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    const loadingToast = toast.loading(`Updating status to ${newStatus}...`);

    const result = await updateDriverStatusAction(user.id, newStatus);
    console.log("Driver Location updating: ", result);
    toast.dismiss(loadingToast);

    if (result.success) {
      setStatus(newStatus);
      toast.success(`Status updated to ${newStatus}`);
      router.refresh();
    } else {
      toast.error("Error updating status: " + result.error);
    }
  };

  const handleAcceptRide = async (rideId: number) => {
    const loadingToast = toast.loading("Accepting ride...");

    const result = await acceptRideAction(rideId, user.id);

    toast.dismiss(loadingToast);

    if (result.success) {
      toast.success("Ride accepted!");
      router.refresh();
    } else {
      toast.error("Error accepting ride: " + result.error);
    }
  };

  const handleStartRide = async (rideId: number) => {
    const loadingToast = toast.loading("Starting ride...");

    const result = await startRideAction(rideId);

    toast.dismiss(loadingToast);

    if (result.success) {
      toast.success("Ride started! Drive safely. 🚗");
      router.refresh();
    } else {
      toast.error("Error starting ride: " + result.error);
    }
  };

  const handleCompleteRide = (rideId: number) => {
    setSelectedRideId(rideId);

    toast(
      (t) => (
        <div className="flex flex-col space-y-3">
          <p className="font-semibold">Complete this ride?</p>
          <div className="flex space-x-2">
            <button
              onClick={() => {
                toast.dismiss(t.id);
                confirmCompleteRide(rideId);
              }}
              className="px-4 py-2 bg-green-500 text-white rounded-lg font-semibold hover:bg-green-600"
            >
              Confirm
            </button>
            <button
              onClick={() => toast.dismiss(t.id)}
              className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg font-semibold hover:bg-gray-400"
            >
              Cancel
            </button>
          </div>
        </div>
      ),
      {
        duration: 10000,
      }
    );
  };

  const confirmCompleteRide = async (rideId: number) => {
    const loadingToast = toast.loading("Completing ride...");

    const result = await completeRideAction(rideId);

    toast.dismiss(loadingToast);

    if (result.success) {
      toast.success("Ride completed! 🎉");
      router.refresh();
    } else {
      toast.error("Error completing ride: " + result.error);
    }
  };

  const handleCancelRide = (rideId: number) => {
    setSelectedRideId(rideId);
    setShowCancelModal(true);
  };

  const confirmCancelRide = async () => {
    if (!cancellationReason.trim()) {
      toast.error("Please provide a reason for cancellation");
      return;
    }

    if (!selectedRideId) return;

    setCancellingRide(selectedRideId);
    setShowCancelModal(false);

    const loadingToast = toast.loading("Cancelling ride...");

    const result = await cancelRideAction(
      selectedRideId,
      user.id,
      cancellationReason
    );

    toast.dismiss(loadingToast);
    setCancellingRide(null);
    setCancellationReason("");
    setSelectedRideId(null);

    if (result.success) {
      toast.success("Ride cancelled");
      router.refresh();
    } else {
      toast.error("Error cancelling ride: " + result.error);
    }
  };

  const renderRideActions = (ride: any) => {
    switch (ride.status) {
      case "REQUESTED":
        return (
          <div className="mt-2 flex space-x-2">
            <button
              onClick={() => handleAcceptRide(ride.id)}
              className="flex-1 bg-green-500 text-white py-2 rounded-lg font-semibold hover:bg-green-600 transition-all"
            >
              Accept Ride
            </button>
          </div>
        );

      case "ACCEPTED":
        return (
          <div className="mt-2 flex space-x-2">
            <button
              onClick={() => handleStartRide(ride.id)}
              className="flex-1 bg-blue-500 text-white py-2 rounded-lg font-semibold hover:bg-blue-600 transition-all"
            >
              Start Ride
            </button>
            <button
              onClick={() => handleCancelRide(ride.id)}
              disabled={cancellingRide === ride.id}
              className="px-6 bg-red-500 text-white py-2 rounded-lg font-semibold hover:bg-red-600 transition-all disabled:opacity-50"
            >
              {cancellingRide === ride.id ? "Cancelling..." : "Cancel"}
            </button>
          </div>
        );

      case "IN_PROGRESS":
        return (
          <div className="mt-2 flex space-x-2">
            <button
              onClick={() => handleCompleteRide(ride.id)}
              className="flex-1 bg-green-500 text-white py-2 rounded-lg font-semibold hover:bg-green-600 transition-all"
            >
              Complete Ride
            </button>
            <button
              onClick={() => handleCancelRide(ride.id)}
              disabled={cancellingRide === ride.id}
              className="px-6 bg-red-500 text-white py-2 rounded-lg font-semibold hover:bg-red-600 transition-all disabled:opacity-50"
            >
              {cancellingRide === ride.id ? "Cancelling..." : "Cancel"}
            </button>
          </div>
        );

      case "COMPLETED":
        return (
          <div className="mt-2">
            <div className="bg-green-100 text-green-700 py-2 px-4 rounded-lg text-center font-semibold">
              ✓ Ride Completed
            </div>
          </div>
        );

      case "CANCELLED":
        return (
          <div className="mt-2">
            <div className="bg-gray-100 text-gray-700 py-2 px-4 rounded-lg text-center font-semibold">
              ✗ Ride Cancelled
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      {/* Toast Container */}
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 3000,
          style: {
            background: "#363636",
            color: "#fff",
          },
          success: {
            duration: 3000,
            iconTheme: {
              primary: "#10b981",
              secondary: "#fff",
            },
          },
          error: {
            duration: 4000,
            iconTheme: {
              primary: "#ef4444",
              secondary: "#fff",
            },
          },
        }}
      />

      {/* Cancellation Modal */}
      {showCancelModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-2xl p-6 w-full max-w-md">
            <h3 className="text-xl font-bold text-gray-800 mb-4">
              Cancel Ride
            </h3>
            <p className="text-gray-600 mb-4">
              Please provide a reason for cancelling this ride:
            </p>
            <textarea
              value={cancellationReason}
              onChange={(e) => setCancellationReason(e.target.value)}
              placeholder="Enter cancellation reason..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              rows={4}
            />
            <div className="flex space-x-3 mt-4">
              <button
                onClick={confirmCancelRide}
                className="flex-1 bg-red-500 text-white py-2 rounded-lg font-semibold hover:bg-red-600 transition-all"
              >
                Confirm Cancel
              </button>
              <button
                onClick={() => {
                  setShowCancelModal(false);
                  setCancellationReason("");
                  setSelectedRideId(null);
                }}
                className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-lg font-semibold hover:bg-gray-400 transition-all"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Driver Status</h2>
        <div className="flex space-x-3 mb-4">
          <button
            onClick={() => handleStatusChange("AVAILABLE")}
            className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
              status === "AVAILABLE"
                ? "bg-green-500 text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            Available
          </button>
          <button
            onClick={() => handleStatusChange("BUSY")}
            className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
              status === "BUSY"
                ? "bg-yellow-500 text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            Busy
          </button>
          <button
            onClick={() => handleStatusChange("OFFLINE")}
            className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
              status === "OFFLINE"
                ? "bg-red-500 text-white"
                : "bg-gray-200 text-gray-700 hover:bg-gray-300"
            }`}
          >
            Offline
          </button>
        </div>
        <button
          onClick={() => setShowLocationModal(true)}
          className="w-full bg-blue-500 text-white py-3 rounded-lg font-semibold hover:bg-blue-600 transition-all flex items-center justify-center space-x-2"
        >
          <MapPin className="w-5 h-5" />
          <span>Update Location</span>
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Your Rides</h2>
        <div className="space-y-4">
          {rides.length === 0 ? (
            <p className="text-gray-600 text-center py-8">
              No rides yet. Set your status to Available!
            </p>
          ) : (
            sortedRides.map((ride) => (
              <div key={ride.id}>
                <RideCard ride={ride} isDriver={true} />
                {renderRideActions(ride)}
              </div>
            ))
          )}
        </div>
      </div>

      {/* Location Update Modal */}
      {showLocationModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-2xl p-6 w-full max-w-2xl">
            <h3 className="text-xl font-bold text-gray-800 mb-4">
              Update Your Location
            </h3>
            <div
              id="location-map"
              className="w-full h-96 rounded-lg border-2 border-gray-200 mb-4"
            />
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Current Location
              </label>
              <div className="flex items-center space-x-2">
                <input
                  type="text"
                  value={currentLocation.address}
                  readOnly
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
                />
                <button
                  onClick={getCurrentLocation}
                  className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                  title="Use current location"
                >
                  <Navigation className="w-5 h-5" />
                </button>
              </div>
              <p className="text-xs text-gray-500 mt-1">
                Lat: {currentLocation.latitude.toFixed(6)}, Lng:{" "}
                {currentLocation.longitude.toFixed(6)}
              </p>
            </div>
            <p className="text-sm text-gray-600 mb-4">
              Click on the map or drag the marker to set your location
            </p>
            <div className="flex space-x-3">
              <button
                onClick={handleUpdateLocation}
                className="flex-1 bg-blue-500 text-white py-2 rounded-lg font-semibold hover:bg-blue-600 transition-all"
              >
                Update Location
              </button>
              <button
                onClick={() => {
                  setShowLocationModal(false);
                  setMapRef(null);
                  setMarkerRef(null);
                }}
                className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-lg font-semibold hover:bg-gray-400 transition-all"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

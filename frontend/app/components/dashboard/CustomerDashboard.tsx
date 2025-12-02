"use client";

import { useState, useEffect, useRef, useMemo } from "react";
import { useRouter } from "next/navigation";
import { Car, MapPin, Navigation, X, AlertCircle } from "lucide-react";
import { requestRideAction, cancelRideAction } from "../../actions/ride";
import RideCard from "../RiderCard";
import toast, { Toaster } from "react-hot-toast";

interface CustomerDashboardProps {
  user: any;
  rides: any[];
}

// Simple Dialog Component
function Dialog({
  open,
  onClose,
  children,
}: {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
}) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/50" onClick={onClose} />

      {/* Dialog Content */}
      <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full mx-4 p-6 animate-in fade-in zoom-in duration-200">
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>
        {children}
      </div>
    </div>
  );
}

export default function CustomerDashboard({
  user,
  rides,
}: CustomerDashboardProps) {
  const router = useRouter();
  const [showRequestForm, setShowRequestForm] = useState(false);
  const [pickingLocation, setPickingLocation] = useState<
    "pickup" | "dropoff" | null
  >(null);
  const [cancellingRide, setCancellingRide] = useState<number | null>(null);
  const [showCancelDialog, setShowCancelDialog] = useState(false);
  const [rideToCancel, setRideToCancel] = useState<number | null>(null);
  const [cancellationReason, setCancellationReason] = useState("");
  const [rideRequest, setRideRequest] = useState({
    pickupLat: -1.2921,
    pickupLng: 36.8219,
    pickupAddress: "Nairobi CBD",
    dropoffLat: -1.2864,
    dropoffLng: 36.8172,
    dropoffAddress: "Westlands",
  });
  const [mapLoaded, setMapLoaded] = useState(false);

  const mapRef = useRef<any>(null);
  const pickupMarkerRef = useRef<any>(null);
  const dropoffMarkerRef = useRef<any>(null);
  const mapContainerRef = useRef<HTMLDivElement>(null);

  // Check if there's a pending ride
  const hasPendingRide = useMemo(() => {
    return rides.some(
      (ride) =>
        ride.status === "REQUESTED" ||
        ride.status === "ACCEPTED" ||
        ride.status === "IN_PROGRESS"
    );
  }, [rides]);

  // Sort rides by most recent first
  const sortedRides = useMemo(() => {
    return [...rides].sort((a, b) => {
      const dateA = new Date(a.createdAt || a.requestedAt || 0).getTime();
      const dateB = new Date(b.createdAt || b.requestedAt || 0).getTime();
      return dateB - dateA; // Most recent first
    });
  }, [rides]);

  useEffect(() => {
    if (showRequestForm && typeof window !== "undefined") {
      loadLeaflet();
    }
  }, [showRequestForm]);

  const loadLeaflet = () => {
    // Load Leaflet CSS
    if (!document.querySelector('link[href*="leaflet.css"]')) {
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      link.integrity = "sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=";
      link.crossOrigin = "";
      document.head.appendChild(link);
    }

    // Load Leaflet JS
    if (!(window as any).L) {
      const script = document.createElement("script");
      script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      script.integrity = "sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=";
      script.crossOrigin = "";
      script.onload = () => {
        setMapLoaded(true);
        initializeMap();
      };
      document.head.appendChild(script);
    } else {
      setMapLoaded(true);
      initializeMap();
    }
  };

  const initializeMap = () => {
    if (!mapContainerRef.current || !(window as any).L) return;
    if (mapRef.current) return;

    const L = (window as any).L;

    const map = L.map(mapContainerRef.current).setView(
      [rideRequest.pickupLat, rideRequest.pickupLng],
      13
    );

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
      maxZoom: 19,
    }).addTo(map);

    mapRef.current = map;

    const pickupIcon = L.divIcon({
      className: "custom-marker",
      html: `<div style="background-color: #10b981; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });

    const dropoffIcon = L.divIcon({
      className: "custom-marker",
      html: `<div style="background-color: #ef4444; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12],
    });

    pickupMarkerRef.current = L.marker(
      [rideRequest.pickupLat, rideRequest.pickupLng],
      {
        icon: pickupIcon,
        draggable: true,
      }
    ).addTo(map);

    dropoffMarkerRef.current = L.marker(
      [rideRequest.dropoffLat, rideRequest.dropoffLng],
      {
        icon: dropoffIcon,
        draggable: true,
      }
    ).addTo(map);

    pickupMarkerRef.current.on("dragend", (e: any) => {
      const pos = e.target.getLatLng();
      updateLocation("pickup", pos.lat, pos.lng);
    });

    dropoffMarkerRef.current.on("dragend", (e: any) => {
      const pos = e.target.getLatLng();
      updateLocation("dropoff", pos.lat, pos.lng);
    });

    map.on("click", (e: any) => {
      if (pickingLocation) {
        const { lat, lng } = e.latlng;

        if (pickingLocation === "pickup") {
          pickupMarkerRef.current.setLatLng([lat, lng]);
          updateLocation("pickup", lat, lng);
        } else {
          dropoffMarkerRef.current.setLatLng([lat, lng]);
          updateLocation("dropoff", lat, lng);
        }

        setPickingLocation(null);
      }
    });
  };

  const updateLocation = async (
    type: "pickup" | "dropoff",
    lat: number,
    lng: number
  ) => {
    if (type === "pickup") {
      setRideRequest((prev) => ({
        ...prev,
        pickupLat: lat,
        pickupLng: lng,
        pickupAddress: "Loading address...",
      }));
    } else {
      setRideRequest((prev) => ({
        ...prev,
        dropoffLat: lat,
        dropoffLng: lng,
        dropoffAddress: "Loading address...",
      }));
    }

    const address = await reverseGeocode(lat, lng);

    if (type === "pickup") {
      setRideRequest((prev) => ({
        ...prev,
        pickupAddress: address,
      }));
    } else {
      setRideRequest((prev) => ({
        ...prev,
        dropoffAddress: address,
      }));
    }
  };

  const reverseGeocode = async (lat: number, lng: number): Promise<string> => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`,
        {
          headers: {
            "User-Agent": "RideSharingApp/1.0",
          },
        }
      );
      const data = await response.json();
      if (data.display_name) {
        return data.display_name;
      }
    } catch (error) {
      console.error("Geocoding error:", error);
      toast.error("Failed to fetch address");
    }
    return `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
  };

  const forwardGeocode = async (
    address: string
  ): Promise<{ lat: number; lng: number } | null> => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
          address
        )}&limit=1`,
        {
          headers: {
            "User-Agent": "RideSharingApp/1.0",
          },
        }
      );
      const data = await response.json();
      if (data && data.length > 0) {
        return {
          lat: parseFloat(data[0].lat),
          lng: parseFloat(data[0].lon),
        };
      }
    } catch (error) {
      console.error("Geocoding error:", error);
      toast.error("Failed to find address");
    }
    return null;
  };

  const handleAddressChange = async (
    type: "pickup" | "dropoff",
    address: string
  ) => {
    if (type === "pickup") {
      setRideRequest((prev) => ({ ...prev, pickupAddress: address }));
    } else {
      setRideRequest((prev) => ({ ...prev, dropoffAddress: address }));
    }

    if (address.length > 5) {
      const coords = await forwardGeocode(address);
      if (coords) {
        if (type === "pickup") {
          setRideRequest((prev) => ({
            ...prev,
            pickupLat: coords.lat,
            pickupLng: coords.lng,
          }));
          pickupMarkerRef.current?.setLatLng([coords.lat, coords.lng]);
        } else {
          setRideRequest((prev) => ({
            ...prev,
            dropoffLat: coords.lat,
            dropoffLng: coords.lng,
          }));
          dropoffMarkerRef.current?.setLatLng([coords.lat, coords.lng]);
        }

        mapRef.current?.panTo([coords.lat, coords.lng]);
      }
    }
  };

  const handlePickLocation = (type: "pickup" | "dropoff") => {
    if (pickingLocation === type) {
      setPickingLocation(null);
    } else {
      setPickingLocation(type);
      if (mapRef.current) {
        const container = mapRef.current.getContainer();
        container.style.cursor = "crosshair";
      }
    }
  };

  useEffect(() => {
    if (!pickingLocation && mapRef.current) {
      const container = mapRef.current.getContainer();
      container.style.cursor = "";
    }
  }, [pickingLocation]);

  const getCurrentLocation = () => {
    if (navigator.geolocation) {
      const loadingToast = toast.loading("Getting your location...");
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;

          setRideRequest((prev) => ({
            ...prev,
            pickupLat: lat,
            pickupLng: lng,
            pickupAddress: "Loading address...",
          }));

          pickupMarkerRef.current?.setLatLng([lat, lng]);
          mapRef.current?.panTo([lat, lng]);

          const address = await reverseGeocode(lat, lng);
          setRideRequest((prev) => ({
            ...prev,
            pickupAddress: address,
          }));

          toast.success("Location updated!", { id: loadingToast });
        },
        (error) => {
          console.error("Error getting location:", error);
          toast.error("Unable to get your current location", {
            id: loadingToast,
          });
        }
      );
    } else {
      toast.error("Geolocation is not supported by your browser");
    }
  };

  const handleShowRequestForm = () => {
    if (hasPendingRide) {
      toast.error(
        "You already have an active or pending ride. Please complete or cancel it first.",
        {
          duration: 4000,
          icon: "⚠️",
        }
      );
      return;
    }
    setShowRequestForm(true);
  };

  const handleRequestRide = async () => {
    if (hasPendingRide) {
      toast.error("You already have an active ride");
      return;
    }

    if (!rideRequest.pickupAddress || !rideRequest.dropoffAddress) {
      toast.error("Please enter both pickup and dropoff addresses");
      return;
    }

    if (
      rideRequest.pickupAddress === "Loading address..." ||
      rideRequest.dropoffAddress === "Loading address..."
    ) {
      toast.error("Please wait for addresses to load");
      return;
    }

    const loadingToast = toast.loading("Requesting ride...");
    const result = await requestRideAction(user.id, rideRequest);

    if (result.success) {
      setShowRequestForm(false);
      toast.success("Ride requested! Waiting for driver acceptance.", {
        id: loadingToast,
        duration: 5000,
      });
      router.refresh();
    } else {
      toast.error(`Error: ${result.error}`, {
        id: loadingToast,
      });
    }
  };

  const openCancelDialog = (rideId: number) => {
    setRideToCancel(rideId);
    setCancellationReason("");
    setShowCancelDialog(true);
  };

  const handleCancelRide = async () => {
    if (!rideToCancel) return;

    setShowCancelDialog(false);
    setCancellingRide(rideToCancel);

    const loadingToast = toast.loading("Cancelling ride...");
    const result = await cancelRideAction(
      rideToCancel,
      user.id,
      cancellationReason || undefined
    );

    setCancellingRide(null);
    setRideToCancel(null);
    setCancellationReason("");

    if (result.success) {
      toast.success("Ride cancelled successfully", { id: loadingToast });
      router.refresh();
    } else {
      toast.error(`Error: ${result.error}`, {
        id: loadingToast,
      });
    }
  };

  const renderRideActions = (ride: any) => {
    switch (ride.status) {
      case "REQUESTED":
        return (
          <div className="mt-2">
            <button
              onClick={() => openCancelDialog(ride.id)}
              disabled={cancellingRide === ride.id}
              className="w-full bg-red-500 text-white py-2 rounded-lg font-semibold hover:bg-red-600 transition-all disabled:opacity-50"
            >
              {cancellingRide === ride.id ? "Cancelling..." : "Cancel Request"}
            </button>
          </div>
        );

      case "ACCEPTED":
        return (
          <div className="mt-2">
            <div className="bg-blue-100 text-blue-700 py-2 px-4 rounded-lg mb-2 text-center font-semibold">
              Driver is on the way!
            </div>
            <button
              onClick={() => openCancelDialog(ride.id)}
              disabled={cancellingRide === ride.id}
              className="w-full bg-red-500 text-white py-2 rounded-lg font-semibold hover:bg-red-600 transition-all disabled:opacity-50"
            >
              {cancellingRide === ride.id ? "Cancelling..." : "Cancel Ride"}
            </button>
          </div>
        );

      case "IN_PROGRESS":
        return (
          <div className="mt-2">
            <div className="bg-green-100 text-green-700 py-2 px-4 rounded-lg text-center font-semibold">
              🚗 Ride in progress - Enjoy your trip!
            </div>
            <p className="text-xs text-gray-500 text-center mt-2">
              You cannot cancel a ride that has already started
            </p>
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
    <>
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

      <div className="max-w-6xl mx-auto space-y-6">
        <div className="bg-white rounded-xl shadow-lg p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">
            Request a Ride
          </h2>

          {/* Pending Ride Warning */}
          {hasPendingRide && (
            <div className="mb-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg flex items-start space-x-3">
              <AlertCircle className="w-5 h-5 text-yellow-600 mt-0.5 flex-shrink-0" />
              <div>
                <p className="text-yellow-800 font-semibold">
                  Active Ride in Progress
                </p>
                <p className="text-yellow-700 text-sm">
                  You have an active or pending ride. Please complete or cancel
                  it before requesting a new one.
                </p>
              </div>
            </div>
          )}

          {!showRequestForm ? (
            <button
              onClick={handleShowRequestForm}
              disabled={hasPendingRide}
              className={`w-full py-4 rounded-lg font-semibold transition-all flex items-center justify-center space-x-2 ${
                hasPendingRide
                  ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                  : "bg-gradient-to-r from-blue-500 to-indigo-600 text-white hover:from-blue-600 hover:to-indigo-700"
              }`}
            >
              <Car className="w-5 h-5" />
              <span>Request New Ride</span>
            </button>
          ) : (
            <div className="space-y-4">
              <div className="relative">
                <div
                  ref={mapContainerRef}
                  className="w-full h-96 rounded-lg border-2 border-gray-200"
                />
                {pickingLocation && (
                  <div className="absolute top-4 left-1/2 transform -translate-x-1/2 bg-blue-600 text-white px-4 py-2 rounded-lg shadow-lg z-[1000] flex items-center space-x-2">
                    <MapPin className="w-5 h-5" />
                    <span>
                      Click on map to set{" "}
                      {pickingLocation === "pickup" ? "pickup" : "dropoff"}{" "}
                      location
                    </span>
                    <button
                      onClick={() => setPickingLocation(null)}
                      className="ml-2 text-white hover:text-gray-200"
                    >
                      ✕
                    </button>
                  </div>
                )}
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Pickup Address
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="text"
                      required
                      className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      value={rideRequest.pickupAddress}
                      onChange={(e) =>
                        handleAddressChange("pickup", e.target.value)
                      }
                      placeholder="Enter pickup location"
                    />
                    <button
                      onClick={() => handlePickLocation("pickup")}
                      className={`p-2 rounded-lg transition-colors ${
                        pickingLocation === "pickup"
                          ? "bg-blue-600 text-white"
                          : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      }`}
                      title="Pick from map"
                    >
                      <MapPin className="w-5 h-5" />
                    </button>
                    <button
                      onClick={getCurrentLocation}
                      className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition-colors"
                      title="Use current location"
                    >
                      <Navigation className="w-5 h-5" />
                    </button>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    {rideRequest.pickupLat.toFixed(6)},{" "}
                    {rideRequest.pickupLng.toFixed(6)}
                  </p>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Dropoff Address
                  </label>
                  <div className="flex space-x-2">
                    <input
                      type="text"
                      required
                      className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                      value={rideRequest.dropoffAddress}
                      onChange={(e) =>
                        handleAddressChange("dropoff", e.target.value)
                      }
                      placeholder="Enter destination"
                    />
                    <button
                      onClick={() => handlePickLocation("dropoff")}
                      className={`p-2 rounded-lg transition-colors ${
                        pickingLocation === "dropoff"
                          ? "bg-blue-600 text-white"
                          : "bg-gray-200 text-gray-700 hover:bg-gray-300"
                      }`}
                      title="Pick from map"
                    >
                      <MapPin className="w-5 h-5" />
                    </button>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    {rideRequest.dropoffLat.toFixed(6)},{" "}
                    {rideRequest.dropoffLng.toFixed(6)}
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-4 text-sm text-gray-600 bg-gray-50 p-3 rounded-lg">
                <div className="flex items-center space-x-2">
                  <div className="w-4 h-4 rounded-full bg-green-500"></div>
                  <span>Pickup Location</span>
                </div>
                <div className="flex items-center space-x-2">
                  <div className="w-4 h-4 rounded-full bg-red-500"></div>
                  <span>Dropoff Location</span>
                </div>
                <span className="text-gray-500">
                  • Drag markers or click buttons to select location
                </span>
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={handleRequestRide}
                  disabled={
                    rideRequest.pickupAddress === "Loading address..." ||
                    rideRequest.dropoffAddress === "Loading address..."
                  }
                  className="flex-1 bg-gradient-to-r from-blue-500 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:from-blue-600 hover:to-indigo-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Confirm Request
                </button>
                <button
                  onClick={() => {
                    setShowRequestForm(false);
                    setPickingLocation(null);
                  }}
                  className="px-6 bg-gray-200 text-gray-700 py-3 rounded-lg font-semibold hover:bg-gray-300 transition-all"
                >
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="bg-white rounded-xl shadow-lg p-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">Your Rides</h2>
          <div className="space-y-4">
            {sortedRides.length === 0 ? (
              <p className="text-gray-600 text-center py-8">
                No rides yet. Request your first ride!
              </p>
            ) : (
              sortedRides.map((ride) => (
                <div key={ride.id}>
                  <RideCard ride={ride} isDriver={false} />
                  {renderRideActions(ride)}
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Simple Cancel Dialog */}
      <Dialog
        open={showCancelDialog}
        onClose={() => setShowCancelDialog(false)}
      >
        <h3 className="text-xl font-bold text-gray-900 mb-2">Cancel Ride?</h3>
        <p className="text-gray-600 mb-4">
          Are you sure you want to cancel this ride? This action cannot be
          undone.
        </p>

        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Reason (optional)
          </label>
          <textarea
            value={cancellationReason}
            onChange={(e) => setCancellationReason(e.target.value)}
            placeholder="Let us know why you're cancelling..."
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            rows={3}
          />
        </div>

        <div className="flex space-x-3">
          <button
            onClick={() => setShowCancelDialog(false)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg font-semibold text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Keep Ride
          </button>
          <button
            onClick={handleCancelRide}
            className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg font-semibold hover:bg-red-600 transition-colors"
          >
            Cancel Ride
          </button>
        </div>
      </Dialog>
    </>
  );
}

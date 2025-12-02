"use client";

import {
  X,
  User,
  Car,
  Phone,
  Mail,
  MapPin,
  Calendar,
  Clock,
} from "lucide-react";

interface DriverDetailsModalProps {
  isOpen: boolean;
  driver: any | null;
  onClose: () => void;
}

export default function DriverDetailsModal({
  isOpen,
  driver,
  onClose,
}: DriverDetailsModalProps) {
  if (!isOpen || !driver) return null;

  const getStatusColor = (status: string) => {
    switch (status) {
      case "AVAILABLE":
        return "bg-green-100 text-green-700 border-green-200";
      case "BUSY":
        return "bg-yellow-100 text-yellow-700 border-yellow-200";
      case "OFFLINE":
        return "bg-red-100 text-red-700 border-red-200";
      default:
        return "bg-gray-100 text-gray-700 border-gray-200";
    }
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center z-[9999] p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black bg-opacity-50"
        onClick={onClose}
      />

      {/* Modal Content */}
      <div className="bg-white rounded-xl shadow-2xl w-full max-w-2xl relative z-10 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b border-gray-200 sticky top-0 bg-white z-10">
          <h2 className="text-2xl font-bold text-gray-800">Driver Details</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 transition-colors rounded-full p-1 hover:bg-gray-100"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {/* Driver Profile Section */}
          <div className="flex items-center space-x-4 p-4 bg-gradient-to-r from-blue-50 to-blue-100 rounded-lg">
            <div className="w-20 h-20 bg-blue-200 rounded-full flex items-center justify-center">
              <User className="w-10 h-10 text-blue-600" />
            </div>
            <div className="flex-1">
              <h3 className="text-2xl font-bold text-gray-800">
                {driver.firstName} {driver.lastName}
              </h3>
              <p className="text-gray-600">Driver ID: #{driver.id}</p>
              <div className="mt-2">
                <span
                  className={`inline-block px-3 py-1 rounded-full text-sm font-semibold border ${getStatusColor(
                    driver.driverStatus
                  )}`}
                >
                  {driver.driverStatus}
                </span>
              </div>
            </div>
          </div>

          {/* Contact Information */}
          <div className="bg-gray-50 rounded-lg p-4">
            <h4 className="text-lg font-semibold text-gray-800 mb-3">
              Contact Information
            </h4>
            <div className="space-y-3">
              <div className="flex items-center space-x-3">
                <Phone className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Phone Number</p>
                  <p className="font-semibold text-gray-800">
                    {driver.phoneNumber || "Not provided"}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <Mail className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Email</p>
                  <p className="font-semibold text-gray-800">
                    {driver.email || "Not provided"}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Vehicle Information */}
          <div className="bg-gray-50 rounded-lg p-4">
            <h4 className="text-lg font-semibold text-gray-800 mb-3">
              Vehicle Information
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-center space-x-3">
                <Car className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Vehicle Type</p>
                  <p className="font-semibold text-gray-800">
                    {driver.vehicleType || "Not specified"}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <MapPin className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">License Plate</p>
                  <p className="font-semibold text-gray-800">
                    {driver.licensePlate || "Not provided"}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Account Information */}
          <div className="bg-gray-50 rounded-lg p-4">
            <h4 className="text-lg font-semibold text-gray-800 mb-3">
              Account Information
            </h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex items-center space-x-3">
                <Calendar className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Joined Date</p>
                  <p className="font-semibold text-gray-800">
                    {driver.createdAt
                      ? new Date(driver.createdAt).toLocaleDateString()
                      : "N/A"}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <Clock className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Last Updated</p>
                  <p className="font-semibold text-gray-800">
                    {driver.updatedAt
                      ? new Date(driver.updatedAt).toLocaleDateString()
                      : "N/A"}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <User className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Account Status</p>
                  <p className="font-semibold text-gray-800">
                    {driver.active ? (
                      <span className="text-green-600">Active</span>
                    ) : (
                      <span className="text-red-600">Suspended</span>
                    )}
                  </p>
                </div>
              </div>
              <div className="flex items-center space-x-3">
                <User className="w-5 h-5 text-gray-500" />
                <div>
                  <p className="text-sm text-gray-600">Role</p>
                  <p className="font-semibold text-gray-800">{driver.role}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Additional Stats (if available) */}
          {(driver.totalRides || driver.rating || driver.earnings) && (
            <div className="bg-gradient-to-r from-green-50 to-green-100 rounded-lg p-4">
              <h4 className="text-lg font-semibold text-gray-800 mb-3">
                Performance Stats
              </h4>
              <div className="grid grid-cols-3 gap-4">
                {driver.totalRides && (
                  <div className="text-center">
                    <p className="text-2xl font-bold text-green-700">
                      {driver.totalRides}
                    </p>
                    <p className="text-sm text-gray-600">Total Rides</p>
                  </div>
                )}
                {driver.rating && (
                  <div className="text-center">
                    <p className="text-2xl font-bold text-green-700">
                      {driver.rating}⭐
                    </p>
                    <p className="text-sm text-gray-600">Rating</p>
                  </div>
                )}
                {driver.earnings && (
                  <div className="text-center">
                    <p className="text-2xl font-bold text-green-700">
                      ${driver.earnings}
                    </p>
                    <p className="text-sm text-gray-600">Total Earnings</p>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex justify-end p-6 border-t border-gray-200 bg-gray-50">
          <button
            onClick={onClose}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-medium"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

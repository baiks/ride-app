"use client";

import { useState, useEffect } from "react";
import toast, { Toaster } from "react-hot-toast";
import {
  User,
  TrendingUp,
  Car,
  DollarSign,
  AlertCircle,
  MapPin,
  Clock,
  Ban,
  CheckCircle,
  X,
} from "lucide-react";

interface AdminDashboardProps {
  drivers: any[];
  rides: any[];
  users: any[];
  stats?: {
    totalRevenue: number;
    activeRides: number;
    completedRides: number;
    totalUsers: number;
  };
  onUserStatusUpdate?: () => void;
}
import { updateUserStatusAction } from "../../actions/user";
import EditUserModal from "../modals/EditUserModal";
import DriverDetailsModal from "../modals/DriverDetailsModal";

export default function AdminDashboard({
  drivers,
  rides = [],
  users = [],
  stats,
  onUserStatusUpdate,
}: AdminDashboardProps) {
  const [selectedTab, setSelectedTab] = useState<
    "overview" | "drivers" | "rides" | "users"
  >("overview");
  const [searchTerm, setSearchTerm] = useState("");
  const [loadingUserId, setLoadingUserId] = useState<number | null>(null);
  const [editingUser, setEditingUser] = useState<any | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDriver, setSelectedDriver] = useState<any | null>(null);
  const [isDriverModalOpen, setIsDriverModalOpen] = useState(false);
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // useEffect to trigger refresh when actions are completed
  useEffect(() => {
    if (refreshTrigger > 0 && onUserStatusUpdate) {
      onUserStatusUpdate();
    }
  }, [refreshTrigger, onUserStatusUpdate]);

  // Handler functions
  const handleSuspendUser = async (userId: number) => {
    setLoadingUserId(userId);
    try {
      const result = await updateUserStatusAction(userId, false);

      if (result.success) {
        console.log("User suspended successfully", result.data);
        toast.success("User suspended successfully");
        // Trigger refresh
        setRefreshTrigger((prev) => prev + 1);
      } else {
        console.error("Failed to suspend user:", result.error);
        toast.error("Failed to suspend user: " + result.error);
      }
    } catch (error) {
      console.error("Error suspending user:", error);
      toast.error("An error occurred while suspending the user");
    } finally {
      setLoadingUserId(null);
    }
  };

  const handleActivateUser = async (userId: number) => {
    setLoadingUserId(userId);
    try {
      const result = await updateUserStatusAction(userId, true);

      if (result.success) {
        console.log("User activated successfully", result.data);
        toast.success("User activated successfully");
        // Trigger refresh
        setRefreshTrigger((prev) => prev + 1);
      } else {
        console.error("Failed to activate user:", result.error);
        toast.error("Failed to activate user: " + result.error);
      }
    } catch (error) {
      console.error("Error activating user:", error);
      toast.error("An error occurred while activating the user");
    } finally {
      setLoadingUserId(null);
    }
  };

  const handleEditUser = (user: any) => {
    setEditingUser(user);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
  };

  const handleUserUpdate = () => {
    // Trigger refresh after user edit
    setRefreshTrigger((prev) => prev + 1);
    handleCloseModal();
  };

  const handleViewDriverDetails = (driver: any) => {
    setSelectedDriver(driver);
    setIsDriverModalOpen(true);
  };

  const handleCloseDriverModal = () => {
    setIsDriverModalOpen(false);
    setSelectedDriver(null);
  };

  // Calculate statistics
  const totalDrivers = drivers.length;
  const availableDrivers = drivers.filter(
    (d) => d.driverStatus === "AVAILABLE"
  ).length;
  const busyDrivers = drivers.filter((d) => d.driverStatus === "BUSY").length;
  const offlineDrivers = drivers.filter(
    (d) => d.driverStatus === "OFFLINE"
  ).length;

  const activeRides = rides.filter((r) => r.status === "IN_PROGRESS").length;
  const pendingRides = rides.filter((r) => r.status === "REQUESTED").length;
  const completedToday = rides.filter((r) => {
    const today = new Date().toDateString();
    return (
      r.status === "COMPLETED" && new Date(r.updatedAt).toDateString() === today
    );
  }).length;

  // Filter functions
  const filteredDrivers = drivers.filter(
    (driver) =>
      `${driver.firstName} ${driver.lastName}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase()) ||
      driver.licensePlate?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredRides = rides.filter(
    (ride) =>
      ride.id.toString().includes(searchTerm) ||
      ride.status.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const filteredUsers = users.filter(
    (user) =>
      `${user.firstName} ${user.lastName}`
        .toLowerCase()
        .includes(searchTerm.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="max-w-7xl mx-auto space-y-6">
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

      {/* Tab Navigation */}
      <div className="bg-white rounded-xl shadow-lg p-4">
        <div className="flex space-x-2 overflow-x-auto">
          <button
            onClick={() => setSelectedTab("overview")}
            className={`px-6 py-2 rounded-lg font-semibold transition-all whitespace-nowrap ${
              selectedTab === "overview"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            Overview
          </button>
          <button
            onClick={() => setSelectedTab("drivers")}
            className={`px-6 py-2 rounded-lg font-semibold transition-all whitespace-nowrap ${
              selectedTab === "drivers"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            Drivers ({totalDrivers})
          </button>
          <button
            onClick={() => setSelectedTab("rides")}
            className={`px-6 py-2 rounded-lg font-semibold transition-all whitespace-nowrap ${
              selectedTab === "rides"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            Rides ({rides.length})
          </button>
          <button
            onClick={() => setSelectedTab("users")}
            className={`px-6 py-2 rounded-lg font-semibold transition-all whitespace-nowrap ${
              selectedTab === "users"
                ? "bg-blue-500 text-white"
                : "bg-gray-100 text-gray-700 hover:bg-gray-200"
            }`}
          >
            Users ({users.length})
          </button>
        </div>
      </div>

      {/* Overview Tab */}
      {selectedTab === "overview" && (
        <>
          {/* Statistics Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
            <div className="bg-gradient-to-br from-blue-500 to-blue-600 rounded-xl shadow-lg p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-blue-100 text-sm font-medium">
                    Total Revenue
                  </p>
                  <p className="text-3xl font-bold mt-2">
                    ${Math.round(stats?.totalRevenue || 0).toLocaleString()}
                  </p>
                </div>
                <DollarSign className="w-12 h-12 text-blue-200" />
              </div>
            </div>

            <div className="bg-gradient-to-br from-green-500 to-green-600 rounded-xl shadow-lg p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-green-100 text-sm font-medium">
                    Active Rides
                  </p>
                  <p className="text-3xl font-bold mt-2">{activeRides}</p>
                </div>
                <Car className="w-12 h-12 text-green-200" />
              </div>
            </div>

            <div className="bg-gradient-to-br from-yellow-500 to-yellow-600 rounded-xl shadow-lg p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-yellow-100 text-sm font-medium">
                    Pending Rides
                  </p>
                  <p className="text-3xl font-bold mt-2">{pendingRides}</p>
                </div>
                <Clock className="w-12 h-12 text-yellow-200" />
              </div>
            </div>

            <div className="bg-gradient-to-br from-purple-500 to-purple-600 rounded-xl shadow-lg p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-purple-100 text-sm font-medium">
                    Completed Today
                  </p>
                  <p className="text-3xl font-bold mt-2">{completedToday}</p>
                </div>
                <CheckCircle className="w-12 h-12 text-purple-200" />
              </div>
            </div>

            <div className="bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg p-6 text-white">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-orange-100 text-sm font-medium">
                    Available Drivers
                  </p>
                  <p className="text-3xl font-bold mt-2">
                    {availableDrivers}/{totalDrivers}
                  </p>
                </div>
                <User className="w-12 h-12 text-orange-200" />
              </div>
            </div>
          </div>

          {/* Driver Status Overview */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              Driver Status
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="border-l-4 border-green-500 bg-green-50 p-4 rounded">
                <p className="text-green-700 font-semibold text-lg">
                  {availableDrivers} Available
                </p>
                <p className="text-green-600 text-sm">Ready for rides</p>
              </div>
              <div className="border-l-4 border-yellow-500 bg-yellow-50 p-4 rounded">
                <p className="text-yellow-700 font-semibold text-lg">
                  {busyDrivers} Busy
                </p>
                <p className="text-yellow-600 text-sm">Currently on rides</p>
              </div>
              <div className="border-l-4 border-red-500 bg-red-50 p-4 rounded">
                <p className="text-red-700 font-semibold text-lg">
                  {offlineDrivers} Offline
                </p>
                <p className="text-red-600 text-sm">Not available</p>
              </div>
            </div>
          </div>

          {/* Recent Activity */}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              Recent Rides
            </h2>
            <div className="space-y-3">
              {rides.slice(0, 5).map((ride) => (
                <div
                  key={ride.id}
                  className="flex items-center justify-between p-4 bg-gray-50 rounded-lg"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                      <Car className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-800">
                        Ride #{ride.id}
                      </p>
                      <p className="text-sm text-gray-600">
                        {ride.pickupLocation} → {ride.dropoffLocation}
                      </p>
                    </div>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-semibold ${
                      ride.status === "COMPLETED"
                        ? "bg-green-100 text-green-700"
                        : ride.status === "IN_PROGRESS"
                        ? "bg-blue-100 text-blue-700"
                        : ride.status === "CANCELLED"
                        ? "bg-red-100 text-red-700"
                        : "bg-yellow-100 text-yellow-700"
                    }`}
                  >
                    {ride.status}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {/* Drivers Tab */}
      {selectedTab === "drivers" && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">Manage Drivers</h2>
            <input
              type="text"
              placeholder="Search drivers..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredDrivers.map((driver) => (
              <div
                key={driver.id}
                className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
              >
                <div className="flex items-center space-x-3 mb-3">
                  <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                    <User className="w-6 h-6 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-semibold text-gray-800">
                      {driver.firstName} {driver.lastName}
                    </p>
                    <p className="text-sm text-gray-600">
                      {driver.vehicleType}
                    </p>
                  </div>
                </div>
                <div className="space-y-2 text-sm">
                  <div className="flex items-center text-gray-600">
                    <Car className="w-4 h-4 mr-2" />
                    {driver.licensePlate}
                  </div>
                  <div className="flex items-center text-gray-600">
                    <User className="w-4 h-4 mr-2" />
                    {driver.phoneNumber}
                  </div>
                  <div className="flex items-center justify-between mt-3">
                    <span
                      className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${
                        driver.driverStatus === "AVAILABLE"
                          ? "bg-green-100 text-green-700"
                          : driver.driverStatus === "BUSY"
                          ? "bg-yellow-100 text-yellow-700"
                          : "bg-red-100 text-red-700"
                      }`}
                    >
                      {driver.driverStatus}
                    </span>
                    <button
                      onClick={() => handleViewDriverDetails(driver)}
                      className="text-blue-600 hover:text-blue-700 text-sm font-medium"
                    >
                      View Details
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Rides Tab */}
      {selectedTab === "rides" && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">All Rides</h2>
            <input
              type="text"
              placeholder="Search rides..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    ID
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Customer
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Route
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Fare
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredRides.map((ride) => (
                  <tr key={ride.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-sm font-medium text-gray-900">
                      #{ride.id}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      {ride.customer?.firstName} {ride.customer?.lastName}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      <div className="flex items-center space-x-1">
                        <MapPin className="w-4 h-4 text-green-500" />
                        <span className="truncate max-w-[150px]">
                          {ride.pickupLocation}
                        </span>
                        <span>→</span>
                        <MapPin className="w-4 h-4 text-red-500" />
                        <span className="truncate max-w-[150px]">
                          {ride.dropoffLocation}
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-sm font-semibold text-gray-900">
                      ${ride.fare}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          ride.status === "COMPLETED"
                            ? "bg-green-100 text-green-700"
                            : ride.status === "IN_PROGRESS"
                            ? "bg-blue-100 text-blue-700"
                            : ride.status === "CANCELLED"
                            ? "bg-red-100 text-red-700"
                            : "bg-yellow-100 text-yellow-700"
                        }`}
                      >
                        {ride.status}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <button className="text-blue-600 hover:text-blue-700 text-sm font-medium">
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Users Tab */}
      {selectedTab === "users" && (
        <div className="bg-white rounded-xl shadow-lg p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-2xl font-bold text-gray-800">Manage Users</h2>
            <input
              type="text"
              placeholder="Search users..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Name
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Email
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Role
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Phone
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left text-xs font-semibold text-gray-600 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {filteredUsers.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3 text-sm font-medium text-gray-900">
                      {user.firstName} {user.lastName}
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      {user.email}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          user.role === "ADMIN"
                            ? "bg-purple-100 text-purple-700"
                            : user.role === "DRIVER"
                            ? "bg-blue-100 text-blue-700"
                            : "bg-gray-100 text-gray-700"
                        }`}
                      >
                        {user.role}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-600">
                      {user.phoneNumber}
                    </td>
                    <td className="px-4 py-3">
                      <span
                        className={`px-3 py-1 rounded-full text-xs font-semibold ${
                          user.active
                            ? "bg-green-100 text-green-700"
                            : "bg-red-100 text-red-700"
                        }`}
                      >
                        {user.active ? "Active" : "Suspended"}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <button
                        onClick={() => handleEditUser(user)}
                        className="text-blue-600 hover:text-blue-700 text-sm font-medium mr-3"
                        disabled={loadingUserId === user.id}
                      >
                        Edit
                      </button>
                      {user.active ? (
                        <button
                          onClick={() => handleSuspendUser(user.id)}
                          disabled={loadingUserId === user.id}
                          className="text-red-600 hover:text-red-700 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {loadingUserId === user.id
                            ? "Processing..."
                            : "Suspend"}
                        </button>
                      ) : (
                        <button
                          onClick={() => handleActivateUser(user.id)}
                          disabled={loadingUserId === user.id}
                          className="text-green-600 hover:text-green-700 text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          {loadingUserId === user.id
                            ? "Processing..."
                            : "Activate"}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      <EditUserModal
        isOpen={isModalOpen}
        user={editingUser}
        onClose={handleCloseModal}
        onUpdate={handleUserUpdate}
      />

      {/* Driver Details Modal */}
      <DriverDetailsModal
        isOpen={isDriverModalOpen}
        driver={selectedDriver}
        onClose={handleCloseDriverModal}
      />
    </div>
  );
}

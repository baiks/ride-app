import { redirect } from "next/navigation";
import { getCurrentUser } from "../actions/auth";
import { getCustomerRides, getDriverRides, getAllRides } from "../actions/ride";
import { getAllDrivers, getAllUsers } from "../actions/user";
import Header from "../components/Header";
import CustomerDashboard from "../components/dashboard/CustomerDashboard";
import DriverDashboard from "../components/dashboard/DriverDashboard";
import AdminDashboard from "../components/dashboard/AdminDashboard";

export default async function DashboardPage() {
  const user = await getCurrentUser();

  if (!user) {
    redirect("/");
  }

  let rides = [];
  let drivers = [];
  let users = [];
  let stats = undefined;

  if (user.role === "CUSTOMER") {
    rides = await getCustomerRides(user.id);
  } else if (user.role === "DRIVER") {
    rides = await getDriverRides(user.id);
  } else if (user.role === "ADMIN") {
    drivers = await getAllDrivers();
    rides = await getAllRides();
    users = await getAllUsers();

    // Calculate admin statistics
    const completedRides = rides.filter((r: any) => r.status === "COMPLETED");
    const totalRevenue = completedRides.reduce(
      (sum: number, ride: any) => sum + (ride.fare || 0),
      0
    );
    const activeRides = rides.filter(
      (r: any) => r.status === "IN_PROGRESS"
    ).length;

    stats = {
      totalRevenue,
      activeRides,
      completedRides: completedRides.length,
      totalUsers: users.length,
    };

    console.log("Drivers:", drivers);
    console.log("Rides:", rides.length);
    console.log("Users:", users.length);
    console.log("Stats:", stats);
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <Header user={user} />
      <main className="container mx-auto px-4 py-8">
        {user.role === "CUSTOMER" && (
          <CustomerDashboard user={user} rides={rides} />
        )}
        {user.role === "DRIVER" && (
          <DriverDashboard user={user} rides={rides} />
        )}
        {user.role === "ADMIN" && (
          <AdminDashboard
            drivers={drivers}
            rides={rides}
            users={users}
            stats={stats}
          />
        )}
      </main>
    </div>
  );
}

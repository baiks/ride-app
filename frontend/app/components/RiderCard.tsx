import { MapPin, Car, User, Clock } from "lucide-react";

interface RideCardProps {
  ride: any;
  isDriver: boolean;
}

export default function RideCard({ ride, isDriver }: RideCardProps) {
  const getStatusColor = (status: string) => {
    switch (status) {
      case "REQUESTED":
        return "bg-yellow-100 text-yellow-700";
      case "ACCEPTED":
        return "bg-blue-100 text-blue-700";
      case "IN_PROGRESS":
        return "bg-purple-100 text-purple-700";
      case "COMPLETED":
        return "bg-green-100 text-green-700";
      case "CANCELLED":
        return "bg-red-100 text-red-700";
      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  return (
    <div className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-3">
        <div>
          <span
            className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${getStatusColor(
              ride.status
            )}`}
          >
            {ride.status}
          </span>
        </div>
        <div className="text-right">
          <p className="text-2xl font-bold text-gray-800">
            ${ride.fare?.toFixed(2) || "0.00"}
          </p>
          <p className="text-sm text-gray-600">
            {ride.distance?.toFixed(2) || "0"} km
          </p>
        </div>
      </div>

      <div className="space-y-2 text-sm">
        <div className="flex items-start space-x-2">
          <MapPin className="w-4 h-4 text-green-600 mt-0.5 flex-shrink-0" />
          <div>
            <p className="font-medium text-gray-700">Pickup</p>
            <p className="text-gray-600">
              {ride.pickupAddress || `${ride.pickupLat}, ${ride.pickupLng}`}
            </p>
          </div>
        </div>

        <div className="flex items-start space-x-2">
          <MapPin className="w-4 h-4 text-red-600 mt-0.5 flex-shrink-0" />
          <div>
            <p className="font-medium text-gray-700">Dropoff</p>
            <p className="text-gray-600">
              {ride.dropoffAddress || `${ride.dropoffLat}, ${ride.dropoffLng}`}
            </p>
          </div>
        </div>

        {isDriver && ride.customer && (
          <div className="flex items-center space-x-2 pt-2 border-t">
            <User className="w-4 h-4 text-gray-600" />
            <p className="text-gray-700">
              Customer: {ride.customer.firstName} {ride.customer.lastName}
            </p>
          </div>
        )}

        {!isDriver && ride.driver && (
          <div className="flex items-center space-x-2 pt-2 border-t">
            <Car className="w-4 h-4 text-gray-600" />
            <p className="text-gray-700">
              Driver: {ride.driver.firstName} {ride.driver.lastName}
            </p>
          </div>
        )}

        <div className="flex items-center space-x-2 text-xs text-gray-500 pt-2">
          <Clock className="w-3 h-3" />
          <span>{new Date(ride.requestedAt).toLocaleString()}</span>
        </div>
      </div>
    </div>
  );
}

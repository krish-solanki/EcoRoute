package com.example.ecoroute.core.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IndiaCities {

    public static class City {
        public final String name, state;
        public final double latitude, longitude;

        public City(String name, String state, double latitude, double longitude) {
            this.name = name;
            this.state = state;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return name + ", " + state;
        }
    }

    public static final List<City> ALL_CITIES = List.of(
            // Maharashtra
            new City("Mumbai", "Maharashtra", 19.0760, 72.8777),
            new City("Pune", "Maharashtra", 18.5204, 73.8567),
            new City("Nagpur", "Maharashtra", 21.1458, 79.0882),
            new City("Nashik", "Maharashtra", 19.9975, 73.7898),
            new City("Thane", "Maharashtra", 19.2183, 72.9781),
            new City("Navi Mumbai", "Maharashtra", 19.0330, 73.0297),
            new City("Chhatrapati Sambhajinagar", "Maharashtra", 19.8762, 75.3433),
            new City("Solapur", "Maharashtra", 17.6599, 75.9064),
            new City("Kolhapur", "Maharashtra", 16.7050, 74.2433),
            new City("Amravati", "Maharashtra", 20.9374, 77.7796),
            new City("Akola", "Maharashtra", 20.7002, 77.0082),
            new City("Nanded", "Maharashtra", 19.1383, 77.3210),
            new City("Sangli", "Maharashtra", 16.8524, 74.5815),
            new City("Jalgaon", "Maharashtra", 21.0077, 75.5626),
            new City("Latur", "Maharashtra", 18.4088, 76.5604),

            // Gujarat
            new City("Ahmedabad", "Gujarat", 23.0225, 72.5714),
            new City("Surat", "Gujarat", 21.1702, 72.8311),
            new City("Vadodara", "Gujarat", 22.3072, 73.1812),
            new City("Rajkot", "Gujarat", 22.3039, 70.8022),
            new City("Gandhinagar", "Gujarat", 23.2156, 72.6369),
            new City("Bhavnagar", "Gujarat", 21.7645, 72.1519),
            new City("Jamnagar", "Gujarat", 22.4707, 70.0577),
            new City("Junagadh", "Gujarat", 21.5222, 70.4579),
            new City("Anand", "Gujarat", 22.5645, 72.9289),
            new City("Nadiad", "Gujarat", 22.6916, 72.8634),
            new City("Morbi", "Gujarat", 22.8119, 70.8236),
            new City("Bharuch", "Gujarat", 21.7051, 72.9959),
            new City("Vapi", "Gujarat", 20.3893, 72.9106),
            new City("Navsari", "Gujarat", 20.9467, 72.9520),
            new City("Porbandar", "Gujarat", 21.6417, 69.6293),

            // Delhi
            new City("New Delhi", "Delhi", 28.6139, 77.2090),

            // Karnataka
            new City("Bengaluru", "Karnataka", 12.9716, 77.5946),
            new City("Mysuru", "Karnataka", 12.2958, 76.6394),
            new City("Mangaluru", "Karnataka", 12.9141, 74.8560),
            new City("Hubballi", "Karnataka", 15.3647, 75.1240),
            new City("Dharwad", "Karnataka", 15.4589, 75.0078),
            new City("Belagavi", "Karnataka", 15.8497, 74.4977),
            new City("Kalaburagi", "Karnataka", 17.3297, 76.8343),
            new City("Davangere", "Karnataka", 14.4644, 75.9218),
            new City("Ballari", "Karnataka", 15.1394, 76.9214),
            new City("Shivamogga", "Karnataka", 13.9299, 75.5681),
            new City("Tumakuru", "Karnataka", 13.3409, 77.1010),
            new City("Udupi", "Karnataka", 13.3409, 74.7421),

            // Telangana
            new City("Hyderabad", "Telangana", 17.3850, 78.4867),
            new City("Warangal", "Telangana", 17.9689, 79.5941),
            new City("Nizamabad", "Telangana", 18.6725, 78.0941),
            new City("Karimnagar", "Telangana", 18.4386, 79.1288),
            new City("Khammam", "Telangana", 17.2473, 80.1514),
            new City("Ramagundam", "Telangana", 18.7550, 79.4740),
            new City("Mahbubnagar", "Telangana", 16.7375, 77.9903),

            // Tamil Nadu
            new City("Chennai", "Tamil Nadu", 13.0827, 80.2707),
            new City("Coimbatore", "Tamil Nadu", 11.0168, 76.9558),
            new City("Madurai", "Tamil Nadu", 9.9252, 78.1198),
            new City("Tiruchirappalli", "Tamil Nadu", 10.7905, 78.7047),
            new City("Salem", "Tamil Nadu", 11.6643, 78.1460),
            new City("Tiruppur", "Tamil Nadu", 11.1085, 77.3411),
            new City("Erode", "Tamil Nadu", 11.3410, 77.7172),
            new City("Vellore", "Tamil Nadu", 12.9165, 79.1325),
            new City("Thoothukudi", "Tamil Nadu", 8.7642, 78.1348),
            new City("Tirunelveli", "Tamil Nadu", 8.7139, 77.7567),
            new City("Dindigul", "Tamil Nadu", 10.3673, 77.9803),
            new City("Thanjavur", "Tamil Nadu", 10.7870, 79.1378),
            new City("Hosur", "Tamil Nadu", 12.7409, 77.8253),
            new City("Nagercoil", "Tamil Nadu", 8.1833, 77.4119),
            new City("Kanchipuram", "Tamil Nadu", 12.8342, 79.7036),

            // Kerala
            new City("Kochi", "Kerala", 9.9312, 76.2673),
            new City("Thiruvananthapuram", "Kerala", 8.5241, 76.9366),
            new City("Kozhikode", "Kerala", 11.2588, 75.7804),
            new City("Thrissur", "Kerala", 10.5276, 76.2144),
            new City("Kollam", "Kerala", 8.8932, 76.6141),
            new City("Kannur", "Kerala", 11.8745, 75.3704),
            new City("Palakkad", "Kerala", 10.7867, 76.6548),
            new City("Alappuzha", "Kerala", 9.4981, 76.3388),
            new City("Kottayam", "Kerala", 9.5916, 76.5222),
            new City("Malappuram", "Kerala", 11.0510, 76.0711),

            // West Bengal
            new City("Kolkata", "West Bengal", 22.5726, 88.3639),
            new City("Siliguri", "West Bengal", 26.7271, 88.3953),
            new City("Durgapur", "West Bengal", 23.5204, 87.3119),
            new City("Asansol", "West Bengal", 23.6739, 86.9524),
            new City("Howrah", "West Bengal", 22.5958, 88.2636),
            new City("Bardhaman", "West Bengal", 23.2324, 87.8615),
            new City("Kharagpur", "West Bengal", 22.3460, 87.2320),
            new City("Haldia", "West Bengal", 22.0667, 88.0698),
            new City("Jalpaiguri", "West Bengal", 26.5167, 88.7333),

            // Rajasthan
            new City("Jaipur", "Rajasthan", 26.9124, 75.7873),
            new City("Jodhpur", "Rajasthan", 26.2389, 73.0243),
            new City("Udaipur", "Rajasthan", 24.5854, 73.7125),
            new City("Kota", "Rajasthan", 25.2138, 75.8648),
            new City("Ajmer", "Rajasthan", 26.4499, 74.6399),
            new City("Bikaner", "Rajasthan", 28.0229, 73.3119),
            new City("Alwar", "Rajasthan", 27.5530, 76.6346),
            new City("Bharatpur", "Rajasthan", 27.2152, 77.5030),
            new City("Sikar", "Rajasthan", 27.6094, 75.1399),
            new City("Pali", "Rajasthan", 25.7711, 73.3234),

            // Uttar Pradesh
            new City("Lucknow", "Uttar Pradesh", 26.8467, 80.9462),
            new City("Kanpur", "Uttar Pradesh", 26.4499, 80.3319),
            new City("Varanasi", "Uttar Pradesh", 25.3176, 82.9739),
            new City("Agra", "Uttar Pradesh", 27.1767, 78.0081),
            new City("Prayagraj", "Uttar Pradesh", 25.4358, 81.8463),
            new City("Noida", "Uttar Pradesh", 28.5355, 77.3910),
            new City("Ghaziabad", "Uttar Pradesh", 28.6692, 77.4538),
            new City("Meerut", "Uttar Pradesh", 28.9845, 77.7064),
            new City("Bareilly", "Uttar Pradesh", 28.3670, 79.4304),
            new City("Aligarh", "Uttar Pradesh", 27.8974, 78.0880),
            new City("Moradabad", "Uttar Pradesh", 28.8386, 78.7733),
            new City("Gorakhpur", "Uttar Pradesh", 26.7606, 83.3732),
            new City("Jhansi", "Uttar Pradesh", 25.4484, 78.5685),
            new City("Mathura", "Uttar Pradesh", 27.4924, 77.6737),
            new City("Ayodhya", "Uttar Pradesh", 26.7922, 82.1998),
            new City("Saharanpur", "Uttar Pradesh", 29.9680, 77.5552),
            new City("Firozabad", "Uttar Pradesh", 27.1592, 78.3957),

            // Madhya Pradesh
            new City("Bhopal", "Madhya Pradesh", 23.2599, 77.4126),
            new City("Indore", "Madhya Pradesh", 22.7196, 75.8577),
            new City("Gwalior", "Madhya Pradesh", 26.2183, 78.1828),
            new City("Jabalpur", "Madhya Pradesh", 23.1815, 79.9864),
            new City("Ujjain", "Madhya Pradesh", 23.1765, 75.7885),
            new City("Sagar", "Madhya Pradesh", 23.8388, 78.7378),
            new City("Satna", "Madhya Pradesh", 24.6005, 80.8322),
            new City("Rewa", "Madhya Pradesh", 24.5362, 81.3037),
            new City("Ratlam", "Madhya Pradesh", 23.3315, 75.0367),
            new City("Dewas", "Madhya Pradesh", 22.9676, 76.0534),

            // Bihar
            new City("Patna", "Bihar", 25.5941, 85.1376),
            new City("Gaya", "Bihar", 24.7955, 84.9994),
            new City("Muzaffarpur", "Bihar", 26.1209, 85.3647),
            new City("Bhagalpur", "Bihar", 25.2425, 86.9842),
            new City("Darbhanga", "Bihar", 26.1542, 85.8918),
            new City("Purnia", "Bihar", 25.7771, 87.4753),
            new City("Bihar Sharif", "Bihar", 25.1972, 85.5230),
            new City("Arrah", "Bihar", 25.5560, 84.6603),

            // Jharkhand
            new City("Ranchi", "Jharkhand", 23.3441, 85.3096),
            new City("Jamshedpur", "Jharkhand", 22.8046, 86.2029),
            new City("Dhanbad", "Jharkhand", 23.7957, 86.4304),
            new City("Bokaro", "Jharkhand", 23.6693, 86.1511),
            new City("Hazaribagh", "Jharkhand", 23.9929, 85.3637),

            // Odisha
            new City("Bhubaneswar", "Odisha", 20.2961, 85.8245),
            new City("Cuttack", "Odisha", 20.4625, 85.8830),
            new City("Rourkela", "Odisha", 22.2604, 84.8536),
            new City("Berhampur", "Odisha", 19.3149, 84.7941),
            new City("Sambalpur", "Odisha", 21.4669, 83.9812),
            new City("Puri", "Odisha", 19.8135, 85.8312),
            new City("Balasore", "Odisha", 21.4942, 86.9316),

            // Punjab
            new City("Amritsar", "Punjab", 31.6340, 74.8723),
            new City("Ludhiana", "Punjab", 30.9010, 75.8573),
            new City("Jalandhar", "Punjab", 31.3260, 75.5762),
            new City("Patiala", "Punjab", 30.3398, 76.3869),
            new City("Bathinda", "Punjab", 30.2110, 74.9455),
            new City("Mohali", "Punjab", 30.7046, 76.7179),
            new City("Pathankot", "Punjab", 32.2643, 75.6421),

            // Haryana
            new City("Gurugram", "Haryana", 28.4595, 77.0266),
            new City("Faridabad", "Haryana", 28.4089, 77.3178),
            new City("Panipat", "Haryana", 29.3909, 76.9635),
            new City("Ambala", "Haryana", 30.3782, 76.7767),
            new City("Hisar", "Haryana", 29.1492, 75.7217),
            new City("Karnal", "Haryana", 29.6857, 76.9905),
            new City("Rohtak", "Haryana", 28.8955, 76.6066),

            // Uttarakhand
            new City("Dehradun", "Uttarakhand", 30.3165, 78.0322),
            new City("Haridwar", "Uttarakhand", 29.9457, 78.1642),
            new City("Roorkee", "Uttarakhand", 29.8543, 77.8880),
            new City("Haldwani", "Uttarakhand", 29.2183, 79.5130),
            new City("Rudrapur", "Uttarakhand", 28.9845, 79.3950),

            // Himachal Pradesh
            new City("Shimla", "Himachal Pradesh", 31.1048, 77.1734),
            new City("Dharamshala", "Himachal Pradesh", 32.2190, 76.3234),
            new City("Mandi", "Himachal Pradesh", 31.7080, 76.9320),
            new City("Solan", "Himachal Pradesh", 30.9045, 77.0967),

            // Chhattisgarh
            new City("Raipur", "Chhattisgarh", 21.2514, 81.6296),
            new City("Bilaspur", "Chhattisgarh", 22.0797, 82.1409),
            new City("Durg", "Chhattisgarh", 21.1904, 81.2849),
            new City("Bhilai", "Chhattisgarh", 21.2094, 81.4285),
            new City("Korba", "Chhattisgarh", 22.3595, 82.7501),
            new City("Jagdalpur", "Chhattisgarh", 19.0748, 82.0080),

            // Assam
            new City("Guwahati", "Assam", 26.1445, 91.7362),
            new City("Silchar", "Assam", 24.8333, 92.7789),
            new City("Dibrugarh", "Assam", 27.4728, 94.9120),
            new City("Jorhat", "Assam", 26.7509, 94.2037),
            new City("Tezpur", "Assam", 26.6528, 92.7926),

            // Goa
            new City("Panaji", "Goa", 15.4909, 73.8278),
            new City("Margao", "Goa", 15.2832, 73.9862),
            new City("Vasco da Gama", "Goa", 15.3860, 73.8440),

            // Other Northeastern states
            new City("Shillong", "Meghalaya", 25.5788, 91.8933),
            new City("Tura", "Meghalaya", 25.5140, 90.2020),
            new City("Imphal", "Manipur", 24.8170, 93.9368),
            new City("Aizawl", "Mizoram", 23.7271, 92.7176),
            new City("Lunglei", "Mizoram", 22.8900, 92.7400),
            new City("Kohima", "Nagaland", 25.6751, 94.1086),
            new City("Dimapur", "Nagaland", 25.9045, 93.7266),
            new City("Agartala", "Tripura", 23.8315, 91.2868),
            new City("Udaipur", "Tripura", 23.5333, 91.4833),
            new City("Itanagar", "Arunachal Pradesh", 27.0844, 93.6053),
            new City("Naharlagun", "Arunachal Pradesh", 27.1047, 93.6952),
            new City("Gangtok", "Sikkim", 27.3389, 88.6065),

            // Union Territories
            new City("Chandigarh", "Chandigarh", 30.7333, 76.7794),
            new City("Srinagar", "Jammu and Kashmir", 34.0837, 74.7973),
            new City("Jammu", "Jammu and Kashmir", 32.7266, 74.8570),
            new City("Leh", "Ladakh", 34.1526, 77.5771),
            new City("Kargil", "Ladakh", 34.5539, 76.1349),
            new City("Puducherry", "Puducherry", 11.9416, 79.8083),
            new City("Port Blair", "Andaman and Nicobar Islands", 11.6234, 92.7265),
            new City("Silvassa", "Dadra and Nagar Haveli and Daman and Diu", 20.2766, 73.0083),
            new City("Daman", "Dadra and Nagar Haveli and Daman and Diu", 20.3974, 72.8328),
            new City("Kavaratti", "Lakshadweep", 10.5667, 72.6417)
    );

    private IndiaCities() {}
}

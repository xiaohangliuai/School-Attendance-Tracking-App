package com.example.showattendance;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Environment;
import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.common.usermodel.HyperlinkType;
import java.io.FileOutputStream;


public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Button buttonShowStudents;
    private Button buttonSignOut;
    private RecyclerView recyclerView;
    private TextView teacherGps;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Student> studentList;
    private StudentAdapter studentAdapter;
    private List<String> classList;
    private Spinner spinnerClasses;
    private CalendarView calendarView;
    private String selectedDate = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        Log.d(TAG, "onCreate: Initializing views");

        buttonShowStudents = findViewById(R.id.buttonShowStudents);
        recyclerView = findViewById(R.id.recyclerView);
        teacherGps = findViewById(R.id.textViewTeacherGps);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        spinnerClasses = findViewById(R.id.spinnerClasses);
        Button exportCSVButton = findViewById(R.id.exportCSVButton);
        ImageButton exportCSVBasedClassIB = findViewById(R.id.exportCsvBasedClassIB);



        calendarView = findViewById(R.id.calendarCV);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Format the selected date to a string (e.g., "yyyy-MM-dd")
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
//            getInformation();
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);

        classList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasses.setAdapter(adapter);

        // Check if the teacher has selected courses; if not, prompt for selection
        checkAndPromptCourseSelection(adapter);

        buttonShowStudents.setOnClickListener(v -> {
            Log.d(TAG, "Show Students button clicked");
            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getInformation();
            }
        });

        // Set an OnClickListener to handle exporting data
        exportCSVButton.setOnClickListener(view -> {

            if (studentList.isEmpty()) {
                Toast.makeText(TeacherActivity.this, "No data to export", Toast.LENGTH_SHORT).show();
                return;
            }
            exportToCsv(studentList, selectedDate);
//            checkDirectoryAccess();

        });

        exportCSVBasedClassIB.setOnClickListener(view -> {
            showExportDialog();
//            exportToExcelForClass();
//            exportToXlsxForClass();
        });

        buttonSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(TeacherActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void exportToCsv(List<Student> students, String selectedDate) {
        // Ensure the directory exists
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AttendanceRecords");
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Failed to create directory");
            return;
        }

        String selectedClass = (String) spinnerClasses.getSelectedItem();
        String fileName = "Attendance_" + selectedClass.replace(" ", "_") + "_" + selectedDate + ".csv";
        File file = new File(directory, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.append("Name,GPS,Distance,Time,Attendance\n");

            // Write data rows
            for (Student student : students) {
                // Construct GPS field with two coordinates
                String gpsField = student.getGpsPoints(); // assuming getGps() returns a string like "40.7454417, -73.9801617"

                // Write each field separated by commas
                String row = String.format("%s,%s,%f,%s,%s\n",
                        student.getName(),
                        gpsField,
                        student.getDistance(),
                        student.getCheckInTime(),
                        student.getAttendance());

                writer.append(row);
            }

            writer.flush();
            Log.d(TAG, "CSV file saved successfully at " + file.getAbsolutePath());
            Toast.makeText(this, "CSV file saved to Downloads/AttendanceRecords", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error saving CSV file", e);
            Toast.makeText(this, "Error saving CSV file", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExportDialog() {
        // Get the selected class
        String selectedClass = (String) spinnerClasses.getSelectedItem();

        // Create a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export Attendance");

        // Create a SpannableString for the message
        String message = "You have selected the class: " + selectedClass +
                ". Please confirm if you want to download the attendance records.";
        SpannableString spannableMessage = new SpannableString(message);

        // Find the start and end indices of selectedClass in the message
        int start = message.indexOf(selectedClass);
        int end = start + selectedClass.length();

        // Apply bold style to selectedClass
        spannableMessage.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the SpannableString as the message of the dialog
        builder.setMessage(spannableMessage);

        // Set positive button (for downloading CSV)
        builder.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call the exportToCsv function to export the data
                exportToXlsxForClass();
            }
        });

        // Set negative button (for canceling)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Show the dialog
        builder.create().show();
    }



    private void exportToXlsxForClass() {
        String selectedClass = (String) spinnerClasses.getSelectedItem();

        // Ensure the directory exists
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AttendanceRecords");
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Failed to create directory");
            return;
        }

        // Set up the file name
        String fileName = "Attendance_" + selectedClass.replace(" ", "_") + ".xlsx";
        File file = new File(directory, fileName);

        // Initialize Firestore and prepare XLSX content
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference attendanceRef = db.collection("Attendance");

        // Map to store attendance data
        Map<String, Map<String, String>> attendanceMap = new HashMap<>();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double teacherLatitude = location.getLatitude();
                double teacherLongitude = location.getLongitude();

                attendanceRef.whereEqualTo("className", selectedClass).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                studentList.clear();
                                // Process each document in the query result
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String firstName = document.getString("firstName");
                                    String lastName = document.getString("lastName");
                                    String fullName = firstName + " " + lastName;
                                    String signInTime = document.getString("signInTime");
                                    String gpsPoints = document.getString("gpsPoints");

                                    String[] gpsParts = gpsPoints.split(", ");
                                    double studentLatitude = Double.parseDouble(gpsParts[0]);
                                    double studentLongitude = Double.parseDouble(gpsParts[1]);

                                    float[] results = new float[1];
                                    Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
                                    double distance = Math.round(results[0] * 100.0) / 100.0;

                                    String attendance = distance < 10 ? "Present" : "Absent";

                                    studentList.add(new Student(fullName, gpsPoints, distance, signInTime, attendance));
                                    Log.d(TAG, "Student added: " + fullName + ", " + gpsPoints + ", " + distance + ", " + attendance + ", " + signInTime);

                                    // Extract the date from sign-in time
                                    String date = "Absent"; // Default value
                                    if (signInTime != null && !signInTime.isEmpty()) {
                                        date = signInTime.split(" ")[0]; // YYYY-MM-DD
                                    }

                                    if (!attendanceMap.containsKey(fullName)) {
                                        attendanceMap.put(fullName, new HashMap<>());
                                    }
                                    Objects.requireNonNull(attendanceMap.get(fullName)).put(date, attendance);
                                }

                                // Write the XLSX file
                                writeXlsxFile(file, attendanceMap, teacherLatitude, teacherLongitude, studentList);
                            } else {
                                Log.e(TAG, "Error fetching data", task.getException());
                            }
                        });
            } else {
                Log.e(TAG, "Failed to get teacher location");
            }
        });
    }



    private void writeXlsxFile(File file, Map<String, Map<String, String>> attendanceMap, double teacherLatitude, double teacherLongitude, List<Student> studentList) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Attendance");

            // Create header row
            XSSFRow headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");

            // Extract dates from the data for header
            Set<String> dates = new TreeSet<>();
            for (Map<String, String> dateMap : attendanceMap.values()) {
                dates.addAll(dateMap.keySet());
            }
            int columnIndex = 1;
            for (String date : dates) {
                headerRow.createCell(columnIndex++).setCellValue(date);
            }

            // Write the data rows
            int rowIndex = 1;
            for (Map.Entry<String, Map<String, String>> entry : attendanceMap.entrySet()) {
                String name = entry.getKey();
                Map<String, String> dateMap = entry.getValue();

                XSSFRow row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(name);

                columnIndex = 1;
                for (String date : dates) {
                    String attendanceStatus = dateMap.getOrDefault(date, "Absent");
                    XSSFCell cell = row.createCell(columnIndex++);

                    // Create a hyperlink to the detailed sheet if the status is "Present" or "Absent"
                    if (attendanceStatus.equals("Present") || attendanceStatus.equals("Absent")) {
                        Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);

                        // Create a detailed sheet name
                        String detailedSheetName = "Details_" + name.replace(" ", "_") + "_" + date;
                        link.setAddress(detailedSheetName + "!A1"); // Link to the detailed sheet

                        cell.setHyperlink(link);
                    }
                    cell.setCellValue(attendanceStatus);
                }
            }

            // Generate detailed sheets
            generateDetailedSheets(workbook, attendanceMap, studentList);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            Log.d(TAG, "XLSX file created: " + file.getAbsolutePath());
            Toast.makeText(this, "XLSX file saved to Downloads/AttendanceRecords", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error writing XLSX file", e);
        }
    }

    private void generateDetailedSheets(XSSFWorkbook workbook, Map<String, Map<String, String>> attendanceMap, List<Student> studentList) {
        for (Map.Entry<String, Map<String, String>> entry : attendanceMap.entrySet()) {
            String name = entry.getKey();
            Map<String, String> dateMap = entry.getValue();

            for (Map.Entry<String, String> dateEntry : dateMap.entrySet()) {
                String date = dateEntry.getKey();
                String attendanceStatus = dateEntry.getValue();

                // Set up detailed sheet name
                String detailedSheetName = "Details_" + name.replace(" ", "_") + "_" + date;
                XSSFSheet detailedSheet = workbook.createSheet(detailedSheetName);

                // Create header row for detailed information
                XSSFRow detailedHeaderRow = detailedSheet.createRow(0);
                detailedHeaderRow.createCell(0).setCellValue("Name");
                detailedHeaderRow.createCell(1).setCellValue("GPS");
                detailedHeaderRow.createCell(2).setCellValue("Distance");
                detailedHeaderRow.createCell(3).setCellValue("Time");
                detailedHeaderRow.createCell(4).setCellValue("Attendance");

                // Find student in studentList to get GPS and distance
                for (Student student : studentList) {
                    if (student.getName().equals(name) && student.getCheckInTime().startsWith(date)) {
                        XSSFRow detailedRow = detailedSheet.createRow(1);
                        detailedRow.createCell(0).setCellValue(name);
                        detailedRow.createCell(1).setCellValue(student.getGpsPoints());
                        detailedRow.createCell(2).setCellValue(student.getDistance());
                        detailedRow.createCell(3).setCellValue(student.getCheckInTime());
                        detailedRow.createCell(4).setCellValue(attendanceStatus);
                        break; // Stop loop once the student is found
                    }
                }
            }
        }
    }


    private void exportToCsvForClass(String selectedClass) {
        // Ensure the directory exists
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AttendanceRecords");
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e(TAG, "Failed to create directory");
            return;
        }

        // Set up the file name
        String fileName = "Attendance_" + selectedClass.replace(" ", "_") + ".csv";
        File file = new File(directory, fileName);

        // Initialize Firestore and prepare CSV content
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference attendanceRef = db.collection("Attendance");

        // Map to store attendance data
        Map<String, Map<String, String>> attendanceMap = new HashMap<>();

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double teacherLatitude = location.getLatitude();
                double teacherLongitude = location.getLongitude();

                attendanceRef.whereEqualTo("className", selectedClass).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Process each document in the query result
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String firstName = document.getString("firstName");
                                    String lastName = document.getString("lastName");
                                    String fullName = firstName + " " + lastName;
                                    String signInTime = document.getString("signInTime");
                                    String gpsPoints = document.getString("gpsPoints");

                                    String[] gpsParts = gpsPoints.split(", ");
                                    double studentLatitude = Double.parseDouble(gpsParts[0]);
                                    double studentLongitude = Double.parseDouble(gpsParts[1]);

                                    float[] results = new float[1];
                                    Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
                                    double distance = Math.round(results[0] * 100.0) / 100.0;

                                    String attendance = distance < 10 ? "Present" : "Absent";

                                    // Extract the date from sign-in time if it's not null
                                    String date = "Absent"; // Default value
                                    if (signInTime != null && !signInTime.isEmpty()) {
                                        date = signInTime.split(" ")[0]; // YYYY-MM-DD
                                    }

                                    if (!attendanceMap.containsKey(fullName)) {
                                        attendanceMap.put(fullName, new HashMap<>());
                                    }
                                    Objects.requireNonNull(attendanceMap.get(fullName)).put(date, attendance);
                                }

                                // Write the CSV file
                                writeCsvFile(file, attendanceMap);
                            } else {
                                Log.e(TAG, "Error fetching data", task.getException());
                            }
                        });
            } else {
                Log.e(TAG, "Failed to get teacher location");
            }
        });
    }

    private void writeCsvFile(File file, Map<String, Map<String, String>> attendanceMap) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write the header row
            writer.append("Name");

            // Extract dates from the data for header
            Set<String> dates = new TreeSet<>();

            for (Map<String, String> dateMap : attendanceMap.values()) {
                dates.addAll(dateMap.keySet());
            }
            for (String date : dates) {
                writer.append(",").append(date);
            }
            writer.append("\n");

            // Write the data rows
            for (Map.Entry<String, Map<String, String>> entry : attendanceMap.entrySet()) {
                String name = entry.getKey();
                Map<String, String> dateMap = entry.getValue();

                writer.append(name);

                for (String date : dates) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        writer.append(",").append(dateMap.getOrDefault(date, "Absent"));
                    }
                }
                writer.append("\n");
            }

            writer.flush();
            Log.d(TAG, "CSV file created: " + file.getAbsolutePath());
            Toast.makeText(this, "CSV file saved to Downloads/AttendanceRecords", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Log.e(TAG, "Error writing CSV file", e);
        }
    }



    private void checkAndPromptCourseSelection(ArrayAdapter<String> adapter) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> teacherClasses = (List<String>) document.get("Classes");
                                if (teacherClasses != null && !teacherClasses.isEmpty()) {
                                    classList.addAll(teacherClasses);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    // No classes selected yet, prompt for course selection
                                    fetchClassesAndShowDialog(adapter);
                                }
                            } else {
                                Log.e(TAG, "No such document");
                                Toast.makeText(TeacherActivity.this, "No user data found. Please contact support.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error fetching user data: ", task.getException());
                            Toast.makeText(TeacherActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(TeacherActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchClassesAndShowDialog(ArrayAdapter<String> adapter) {
        Log.d(TAG, "Fetching all possible classes from Firestore");
        db.collection("Classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> allClasses = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String className = document.getString("name");
                    if (className != null) {
                        allClasses.add(className);
                    }
                }
                Log.d(TAG, "All classes fetched successfully: " + allClasses);

                // Save allClasses to be used in dialog
                classList.clear();
                classList.addAll(allClasses);

                // Show class selection dialog
                showClassSelectionDialog(adapter);
            } else {
                Log.e(TAG, "Error fetching classes: ", task.getException());
                Toast.makeText(TeacherActivity.this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClassSelectionDialog(ArrayAdapter<String> adapter) {
        if (classList.isEmpty()) {
            Toast.makeText(TeacherActivity.this, "No classes available to select.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Classes");

        String[] classArray = classList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[classArray.length];
        ArrayList<String> selectedItems = new ArrayList<>();

        builder.setMultiChoiceItems(classArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedItems.add(classArray[which]);
            } else {
                selectedItems.remove(classArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (selectedItems.size() != 0) {
                saveSelectedClasses(selectedItems);
            } else {
                Toast.makeText(TeacherActivity.this, "Please at least select 1 class", Toast.LENGTH_SHORT).show();
                showClassSelectionDialog(adapter);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void saveSelectedClasses(List<String> selectedClasses) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid()).update("Classes", selectedClasses)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Classes updated successfully");
                        classList.clear();
                        classList.addAll(selectedClasses);
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerClasses.getAdapter();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(TeacherActivity.this, "Classes selected successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating classes", e);
                        Toast.makeText(TeacherActivity.this, "Failed to update classes", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(TeacherActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void getInformation() {

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Selected Date: " + selectedDate);

        Log.d(TAG, "Getting information...");
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double teacherLatitude = location.getLatitude();
                    double teacherLongitude = location.getLongitude();
                    teacherGps.setText("Latitude: " + teacherLatitude + ", Longitude: " + teacherLongitude);
                    Log.d(TAG, "Teacher location: " + teacherLatitude + ", " + teacherLongitude);
                    fetchStudentData(teacherLatitude, teacherLongitude);
                } else {
                    Log.e(TAG, "Location is null");
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    private void fetchStudentData(double teacherLatitude, double teacherLongitude) {
        Log.d(TAG, "Fetching student data...");
        String selectedClass = (String) spinnerClasses.getSelectedItem();

        // Check if selectedDate is empty or not set
        if (selectedDate.isEmpty()) {
            Log.e(TAG, "SelectedDate is empty");
            return;
        }

        // Convert the selectedDate to the required Firestore string format
        String startOfDayString = selectedDate + " 00:00:00";
        String endOfDayString = selectedDate + " 23:59:59";

        Log.d(TAG, "Start time string: " + startOfDayString);
        Log.d(TAG, "End time string: " + endOfDayString);
        Log.d(TAG, "SelectedClass is: " + selectedClass);

        // Query Firestore
        db.collection("Attendance")
                .whereEqualTo("className", selectedClass)
                .whereGreaterThanOrEqualTo("signInTime", startOfDayString)
                .whereLessThanOrEqualTo("signInTime", endOfDayString)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            Log.d(TAG, "Query successful, documents count: " + task.getResult().size());
                            studentList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userEmail = document.getString("email");
                                String firstName = document.getString("firstName");
                                String lastName = document.getString("lastName");
                                String gpsPoints = document.getString("gpsPoints");
                                String signInTime = document.getString("signInTime");

                                if (gpsPoints != null && !gpsPoints.isEmpty()) {
                                    String[] gpsPointsArray = gpsPoints.split(", ");
                                    double studentLatitude = Double.parseDouble(gpsPointsArray[0]);
                                    double studentLongitude = Double.parseDouble(gpsPointsArray[1]);

                                    float[] results = new float[1];
                                    Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
                                    double distance = Math.round(results[0] * 100.0) / 100.0;
                                    String attendance = distance < 10 ? "Present" : "Absent";

                                    String fullName = firstName + " " + lastName;
                                    studentList.add(new Student(fullName, gpsPoints, distance, signInTime, attendance));
                                    Log.d(TAG, "Student added: " + fullName + ", " + gpsPoints + ", " + distance + ", " + attendance + ", " + signInTime);
                                } else {
                                    Log.d(TAG, "Student " + userEmail + " has no GPS points");
                                }
                            }
                            studentAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No documents found for the query");
                        }
                    } else {
                        Log.e(TAG, "Error getting attendance records: ", task.getException());
                        Toast.makeText(TeacherActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}

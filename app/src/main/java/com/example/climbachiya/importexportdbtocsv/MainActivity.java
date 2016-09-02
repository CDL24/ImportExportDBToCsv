package com.example.climbachiya.importexportdbtocsv;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;
import database.DBController;
import utilities.MarshMallowPermission;

public class MainActivity extends ListActivity {

    TextView lbl;
    DBController controller = new DBController(this);
    Button btnimport;
    ListView lv;
    final Context context = this;
    ListAdapter adapter;
    ArrayList<HashMap<String, String>> myList;
    public static final int requestcode = 1;
    MarshMallowPermission mPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iniUIControls();
        initClassObjects();
    }

    private void initClassObjects() {
        mPermission = new MarshMallowPermission(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!mPermission.checkPermissionForExternalStorage()) {
                mPermission.requestForExternalStorage();
            }
        }

    }
    private void iniUIControls() {
        lbl = (TextView) findViewById(R.id.txtresulttext);
        btnimport = (Button) findViewById(R.id.btnupload);
        lv = getListView();

        btnimport.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("gagt/sdf");
                try {
                    startActivityForResult(fileintent, requestcode);
                } catch (ActivityNotFoundException e) {
                    lbl.setText("No activity can handle picking a file. Showing alternatives.");
                }

            }
        });
        myList = controller.getAllProducts();
        if (myList.size() != 0) {
            ListView lv = getListView();
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, myList,
                    R.layout.single_cell, new String[]{"Company", "Name", "Price"}, new int[]{
                    R.id.txtproductcompany, R.id.txtproductname, R.id.txtproductprice});
            setListAdapter(adapter);
            lbl.setText("");
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        switch (requestCode) {
            case requestcode:
                String filepath = data.getData().getPath();

                /*controller = new DBController(getApplicationContext());
                SQLiteDatabase db = controller.getWritableDatabase();
                String tableName = "proinfo";
                db.execSQL("delete from " + tableName);*/
                try {
                    if (resultCode == RESULT_OK) {
                        try {
                            Log.v("Result :: ", "OK");

                            Log.e("onActivityResult :: "," filepath : "+filepath);
                            String filename = filepath.substring(filepath.lastIndexOf("/")+1);
                            Log.e("onActivityResult :: "," filename : "+filename);

                            if(null != filename && filename.endsWith(".csv")){
                                //Toast.makeText(MainActivity.this, "CSV File", Toast.LENGTH_SHORT).show();
                                new ImportCSV(filepath).execute();
                            }else{
                                ListView lv = getListView();
                                setListAdapter(null);

                                Toast.makeText(MainActivity.this, "Not CSV File", Toast.LENGTH_SHORT).show();
                            }
                            /*FileReader file = new FileReader(filepath);

                            BufferedReader buffer = new BufferedReader(file);
                            ContentValues contentValues = new ContentValues();
                            String line = "";
                            db.beginTransaction();

                            while ((line = buffer.readLine()) != null) {

                                String[] str = line.split(",", 3);  // defining 3 columns with null or blank field //values acceptance
                                //Id, Company,Name,Price
                                String company = str[0].toString();
                                String Name = str[1].toString();
                                String Price = str[2].toString();


                                contentValues.put("Company", company);
                                contentValues.put("Name", Name);
                                contentValues.put("Price", Price);
                                db.insert(tableName, null, contentValues);
                                lbl.setText("Successfully Updated Database.");
                            }
                            db.setTransactionSuccessful();
                            db.endTransaction();*/
                        } catch (Exception e) {
                            /*if (db.inTransaction())
                                db.endTransaction();
                            Dialog d = new Dialog(this);
                            d.setTitle(e.getMessage().toString() + "first");
                            d.show();*/
                            // db.endTransaction();
                        }
                    } else {
                        /*if (db.inTransaction())
                            db.endTransaction();
                        Log.v("Result :: ", "CAncel");
                        Dialog d = new Dialog(this);
                        d.setTitle("Only CSV files allowed");
                        d.show();*/

                        Toast.makeText(MainActivity.this, "Not picking any file", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    /*if (db.inTransaction())
                        db.endTransaction();

                    Dialog d = new Dialog(this);
                    d.setTitle(ex.getMessage().toString() + "second");
                    d.show();*/

                    ex.printStackTrace();
                    // db.endTransaction();
                }
        }
        /*myList = controller.getAllProducts();

        if (myList.size() != 0) {
            ListView lv = getListView();
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, myList,
                    R.layout.single_cell, new String[]{"Company", "Name", "Price"}, new int[]{
                    R.id.txtproductcompany, R.id.txtproductname, R.id.txtproductprice});
            setListAdapter(adapter);
            lbl.setText("Data Imported");
        }*/
    }

    public void onExportToCSV(View view){
        new ExportCSV().execute();
    }

    /**
     * Export SQlite database data into CSV file
     */
    class ExportCSV extends AsyncTask<String, String, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Taking backup...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int result = 0;
            String PACKAGE_NAME = getApplicationContext().getPackageName();
            String currentDBPath = "//data//"+ PACKAGE_NAME +"//databases//"+DBController.DATABASE_NAME;

            try {

                File exportDir = new File(Environment.getExternalStorageDirectory(), "");
                if (!exportDir.exists())
                {
                    exportDir.mkdirs();
                }

                File file = new File(exportDir, "backup_data_sheet.csv");
                file.createNewFile();
                if(file.exists()){

                    CSVWriter csvWrite = new CSVWriter(new FileWriter(file));

                    String selectQuery = "SELECT  * FROM proinfo";
                    SQLiteDatabase database = controller.getWritableDatabase();
                    Cursor cursor = database.rawQuery(selectQuery, null);

                    //csvWrite.writeNext(cursor.getColumnNames());
                    while(cursor.moveToNext())
                    {
                        //Which column you want to export

                        String company = cursor.getString(cursor.getColumnIndex("Company"));
                        String name = cursor.getString(cursor.getColumnIndex("Name"));
                        String price = cursor.getString(cursor.getColumnIndex("Price"));

                        company = company.replace("\"", "");
                        name = name.replace("\"", "");
                        price = price.replace("\"", "");

                        Log.v("company", company);
                        Log.v("name", name);
                        Log.v("price", price);
                        Log.e("--------", "--------");

                        String arrStr[] ={company,
                                name,
                                price};
                        csvWrite.writeNext(arrStr);
                    }
                    csvWrite.close();
                    cursor.close();

                    result = 1;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if(result > 0){
                Toast.makeText(getApplicationContext(), "Backup Successful!",
                        Toast.LENGTH_SHORT).show();

            }else{
                Toast.makeText(getApplicationContext(), "Backup Failed!", Toast.LENGTH_SHORT)
                        .show();
            }

        }
    }

    /**
     * Import CSV file data into Sqlite database table
     */
    class ImportCSV extends AsyncTask<String, String, Integer> {

        String filePath = null;

        public ImportCSV(String filepath) {
            this.filePath = filepath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Importing CSV...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            int result = 0;
            try{

                controller = new DBController(getApplicationContext());
                SQLiteDatabase db = controller.getWritableDatabase();
                String tableName = "proinfo";
                db.execSQL("delete from " + tableName);

                FileReader file = new FileReader(filePath);

                BufferedReader buffer = new BufferedReader(file);
                ContentValues contentValues = new ContentValues();
                String line = "";
                db.beginTransaction();

                while ((line = buffer.readLine()) != null) {

                    String[] str = line.split(",", 3);  // defining 3 columns with null or blank field //values acceptance
                    //Id, Company,Name,Price
                    String company = str[0].toString();
                    String Name = str[1].toString();
                    String Price = str[2].toString();

                    //Removing double quotes from data
                    company = company.replace("\"", "");
                    Name = Name.replace("\"", "");
                    Price = Price.replace("\"", "");

                    Log.v("company", company);
                    Log.v("name", Name);
                    Log.v("price", Price);
                    Log.e("--------", "--------");

                    contentValues.put("Company", company);
                    contentValues.put("Name", Name);
                    contentValues.put("Price", Price);
                    db.insert(tableName, null, contentValues);

                }
                db.setTransactionSuccessful();
                db.endTransaction();

                result = 1;
            }catch (Exception e){

            }finally {
                if(null != controller)
                    controller.close();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if(result > 0){
                Toast.makeText(getApplicationContext(), "Imported Successful!",
                        Toast.LENGTH_SHORT).show();
                lbl.setText("Successfully Updated Database.");

                myList = controller.getAllProducts();

                if (myList.size() != 0) {
                    ListView lv = getListView();
                    ListAdapter adapter = new SimpleAdapter(MainActivity.this, myList,
                            R.layout.single_cell, new String[]{"Company", "Name", "Price"}, new int[]{
                            R.id.txtproductcompany, R.id.txtproductname, R.id.txtproductprice});
                    setListAdapter(adapter);
                    lbl.setText("Data Imported");
                }

            }else{
                ListView lv = getListView();
                setListAdapter(null);
                Toast.makeText(getApplicationContext(), "Importing Failed!", Toast.LENGTH_SHORT)
                        .show();
                lbl.setText("Importing failed.");
            }

        }
    }
}
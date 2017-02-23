package webskitters.com.stockup.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import webskitters.com.stockup.ProductDetailsActivity;
import webskitters.com.stockup.R;

public class Utils {
	static final int REQUEST_CODE_MY_PICK = 1;
	Context mContext;
	Typeface typeFaceSegoeuiReg, typeFaceSegoeuiBold;
	public Utils(Context mContext)
	{
		this.mContext = mContext;
		typeFaceSegoeuiReg = Typeface.createFromAsset(mContext.getAssets(),
				"Roboto-Regular.ttf");
		typeFaceSegoeuiBold = Typeface.createFromAsset(mContext.getAssets(),
				"ROBOTO-BOLD_0.TTF");
	}

	//need to check...its giving java.lang.ArrayIndexOutOfBoundsException
	public String getCapitalizedSentence(String text)
	{
		//remove all white spaces by no character
//		String str=text.replaceAll("\\s+", "");
		String str=text;

		try
		{
			if(str.contains(".") /*&& str.substring(str.lastIndexOf(".")+1).matches("[a-z]")*/)
			{
				//split text by separator
				String[] separated_texts=str.split("\\.");

				String first_trimmed_text=separated_texts[0].trim();
				separated_texts[0]=first_trimmed_text;

				if(separated_texts.length>1)
				{
					//start capitalization from second sentence
					for(int i=1;i<separated_texts.length;i++)
					{
						//convert the text into string array of each character in the sentence
						String[] original_text=separated_texts[i].split("");

						char temp_char=Character.toUpperCase(original_text[1].charAt(0));
						original_text[1]=temp_char+"";

						separated_texts[i]=TextUtils.join("", original_text);
					}
					str=TextUtils.join(".", separated_texts);
				}
			}
		}
		catch(Exception e)
		{
			str=text;
		}

		return str;
	}
	public String getPath(Uri uri) {

		String[] projection = { MediaStore.Images.Media.DATA };
		try{
			String StringPath=uri.toString();
			if(StringPath.contains(":/"))
			{
				Cursor cursor =  ((Activity) mContext).managedQuery(uri, projection, null, null, null);
//			 mAct.startManagingCursor(cursor);
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				return cursor.getString(column_index);
			}else
			{
				return uri.toString();
			}

		}catch(Exception e){
			e.printStackTrace();

		}
		return uri.toString();
	}
	public String getPathFromURI(Uri contentUri)
	{
		try
		{
			String[] proj = {MediaStore.Images.Media.DATA};
			Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null, null, null);
			cursor.moveToFirst();
			int column_index = cursor.getColumnIndex(proj[0]);
			String path=cursor.getString(column_index);
			cursor.close();
			return path;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public void deleteAllFilesFromFolder(File folder_name)
	{
		if (folder_name.isDirectory())
		{
			String[] children = folder_name.list();
			for (int i = 0; i < children.length; i++)
			{
				new File(folder_name, children[i]).delete();
			}
		}
	}

	public String getCopiedFilePath(String file_path,String folder_name)
	{
		String image_path=null;
		InputStream in = null;
		OutputStream out = null;

		String directory_path=Environment.getExternalStorageDirectory().toString()+ "/"+folder_name;
		File myDir = new File(directory_path);
		myDir.mkdirs();

		String file_name = file_path.substring(file_path.lastIndexOf("/")+1);
		File file = new File (myDir, file_name);
		if (file.exists ())
			file.delete ();

		try
		{
			in = new FileInputStream(file_path);
			out = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1)
			{
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;

			// write the output file (You have now copied the file)
			out.flush();
			out.close();
			out = null;

			image_path=directory_path+"/"+file_name;
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return image_path;
	}

	public String getImagePathFromBitmap(Bitmap bitmap_to_image_file,String specific_folder_name)
	{
		String image_path=null;

		String directory_path=Environment.getExternalStorageDirectory().toString()+ "/"+specific_folder_name;
		File myDir = new File(directory_path);
		myDir.mkdirs();

		String file_name = "Image-"+ System.currentTimeMillis() +".jpeg";
		File file = new File (myDir, file_name);
		if (file.exists ())
			file.delete ();
		try
		{
			FileOutputStream out = new FileOutputStream(file);
			bitmap_to_image_file.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();

			image_path=directory_path+"/"+file_name;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return image_path;

	}
	//resize bitmap according to your height & width
	public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight)
	{
		int width = bm.getWidth();

		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;

		float scaleHeight = ((float) newHeight) / height;

		// CREATE A MATRIX FOR THE MANIPULATION

		Matrix matrix = new Matrix();

		// RESIZE THE BIT MAP

		matrix.postScale(scaleWidth, scaleHeight);

		// RECREATE THE NEW BITMAP

		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

		return resizedBitmap;

	}

	//resize bitmap from byte array according to your height & width
	public Bitmap resizeByteArrayImage(byte[] byte_image_array,int req_width,int req_height)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;

		BitmapFactory.decodeByteArray(byte_image_array, 0, byte_image_array.length,options);

		int photoW = options.outWidth;
		int photoH = options.outHeight;

		int scale = 1;
		while (true)
		{
			if (photoW <= req_width && photoH <= req_height)
				break;
			photoW /= 2;
			photoH /= 2;
			scale *= 2;
		}

		// Decode the image file into a Bitmap sized to fill the View
		options.inJustDecodeBounds = false;
		options.inSampleSize = scale;
		options.inPurgeable = true;

		Bitmap resized_bitmap=BitmapFactory.decodeByteArray(byte_image_array, 0, byte_image_array.length, options);

		return resized_bitmap;
	}

	//resize bitmap from image path according to your height & width
	public Bitmap resizeImageFromPath(String currentPhotoPath,int fix_width,int fix_height)
	{
		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;

		BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scale = 1;
		while (true)
		{
			if (photoW <= fix_height && photoH <= fix_height)
				break;
			photoW /= 2;
			photoH /= 2;
			scale *= 2;
		}

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scale;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

		return bitmap;
	}

	//permanently delete image from mobile
	public void deleteImage(String path)
	{
		ContentResolver cr=mContext.getContentResolver();
		cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				MediaStore.MediaColumns.DATA + "='" + path + "'", null);
	}



	//convert bitmap to byte array
	public byte[] getByteArrayFromImage(Bitmap bitmap)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	//get orientation of the photo taken from camera
	public int getCameraRotation(String imagePath)
	{
		int rotate = 0;
		try
		{
			File imageFile = new File(imagePath);

			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation)
			{
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;

				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;

				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;

				default :
					rotate = 0;
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return rotate;
	}

	public int getImageFileRotation(File image_file)
	{
		int rotate = 0;
		try
		{
			ExifInterface exif = new ExifInterface(image_file.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation)
			{
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotate = 270;
					break;

				case ExifInterface.ORIENTATION_ROTATE_180:
					rotate = 180;
					break;

				case ExifInterface.ORIENTATION_ROTATE_90:
					rotate = 90;
					break;

				default :
					rotate = 0;
					break;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return rotate;
	}

	public float getPixelDensity()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.density;
	}

	public float getPixelDensityDpi()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.densityDpi;
	}

	public float getxdpi()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.xdpi;
	}

	public float getydpi()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.ydpi;
	}

	public float getheightPixel()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}

	public float getwidthPixel()
	{
		DisplayMetrics metrics = new DisplayMetrics();
		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	public int getDisplayHeight()
	{
		return mContext.getResources().getDisplayMetrics().heightPixels;
	}

	public int getDisplayWidth()
	{

		return mContext.getResources().getDisplayMetrics().widthPixels;
	}

	/**
	 * checks if internet connection is possible. checks radio signal presence 
	 * and airplane mode.
	 *
	 * @return true if device is connect able.
	 */

	public  boolean isConnectionPossible()
	{
		ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null)
		{
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED)
					{
						return true;
					}

		}
		return false;
	}

	public static boolean isEmailValid(String email)
	{
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches())
		{
			isValid = true;
		}
		return isValid;
	}

	public void displayAlert(String message)
	{
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		//alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setTitle("Stockup") ;
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		TextView myMsg = new TextView(mContext);
		myMsg.setText(message);
		myMsg.setPadding(20, 25, 20, 10);
		myMsg.setTextSize(16);
		myMsg.setTypeface(typeFaceSegoeuiReg);
		myMsg.setTextColor(Color.BLACK);
		myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
		alertDialogBuilder.setView(myMsg);

		TextView title = new TextView(mContext);
		// You Can Customise your Title here
		title.setText("Stockup");
		title.setVisibility(View.GONE);
		title.setBackgroundColor(Color.TRANSPARENT);
		title.setPadding(15, 20, 15, 10);
		title.setGravity(Gravity.CENTER);
		title.setTextColor(Color.BLACK);
		title.setTypeface(typeFaceSegoeuiBold);
		title.setTextSize(20);

		myMsg.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		LinearLayout.LayoutParams positiveButtonLLl = (LinearLayout.LayoutParams) myMsg.getLayoutParams();
		positiveButtonLLl.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
		myMsg.setLayoutParams(positiveButtonLLl);

		alertDialogBuilder.setCustomTitle(title);
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();



		final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
		positiveButton.setTextColor(Color.parseColor("#048BCD"));
		positiveButton.setLayoutParams(positiveButtonLL);

	}
	public void displayAlertWithAnotherTitle(String titleName, String message)
	{
		// TODO Auto-generated method stub
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
		//alertDialogBuilder.setMessage(message);
		alertDialogBuilder.setTitle("Stockup") ;
		alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		TextView myMsg = new TextView(mContext);
		myMsg.setText(message);
		myMsg.setPadding(20, 20, 20, 20);
		myMsg.setTextSize(16);
		myMsg.setTypeface(typeFaceSegoeuiReg);
		myMsg.setTextColor(Color.BLACK);
		myMsg.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL);
		alertDialogBuilder.setView(myMsg);

		TextView title = new TextView(mContext);
		// You Can Customise your Title here
		title.setText(titleName);
		title.setBackgroundColor(Color.TRANSPARENT);
		title.setPadding(15, 20, 15, 10);
		title.setGravity(Gravity.CENTER);
		title.setTextColor(Color.BLACK);
		title.setTypeface(typeFaceSegoeuiBold);
		title.setTextSize(20);
		myMsg.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		LinearLayout.LayoutParams positiveButtonLLl = (LinearLayout.LayoutParams) myMsg.getLayoutParams();
		positiveButtonLLl.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;
		myMsg.setLayoutParams(positiveButtonLLl);

		alertDialogBuilder.setCustomTitle(title);
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

		lp.copyFrom(alertDialog.getWindow().getAttributes());
		lp.width = 600;
		lp.height = 800;
		lp.x=-170;
		lp.y=100;
		alertDialog.getWindow().setAttributes(lp);
		//alertDialog.getWindow().setLayout(600, 1000);
		alertDialog.show();

		final Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT));
		LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
		positiveButtonLL.gravity = Gravity.CENTER|Gravity.CENTER_VERTICAL;

		positiveButton.setTextColor(Color.parseColor("#048BCD"));
		positiveButton.setLayoutParams(positiveButtonLL);
	}

	public void disconnectFromFacebook() {

		if (AccessToken.getCurrentAccessToken() == null) {
			return; // already logged out
		}

		new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
				.Callback() {
			@Override
			public void onCompleted(GraphResponse graphResponse) {

				LoginManager.getInstance().logOut();

			}
		}).executeAsync();
	}
	public static void trackEvent(Context context, String eventName, Map eventValues){
		AppsFlyerLib.getInstance().trackEvent(context, eventName,eventValues);
	}

	public static boolean checkWorkingHour(){
		TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
		Calendar c = Calendar.getInstance(tz);
		String dayLongName = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
				String.format("%02d" , c.get(Calendar.MINUTE))+":"+
				String.format("%02d" , c.get(Calendar.SECOND))+":"+
				String.format("%03d" , c.get(Calendar.MILLISECOND));
		String[] str=time.split(":");
		int timeToCheck=Integer.parseInt(str[0]);
		if(timeToCheck>=23){
			timeToCheck=-1;
		}
		if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=22)&&(dayLongName.equalsIgnoreCase("Monday")||dayLongName.equalsIgnoreCase("Tuesday")
				||dayLongName.equalsIgnoreCase("Wednesday")||dayLongName.equalsIgnoreCase("Thursday")||dayLongName.equalsIgnoreCase("Friday"))){
			return true;
		}/*else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=20)&&(dayLongName.equalsIgnoreCase("Saturday"))){

			*//*if((Integer.parseInt(str[0])<9)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=16)&&(Integer.parseInt(str[1])>=30)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=17)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else{
				return false;
			}*//*
			return  true;

		}*/
		else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=20)&&(dayLongName.equalsIgnoreCase("Sunday")||dayLongName.equalsIgnoreCase("Saturday"))){

			return true;

		}else{
			return  false;
		}
	}

	public static boolean checkWorkingHour(String delivery_type){
		TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
		Calendar c = Calendar.getInstance(tz);
		String dayLongName = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
				String.format("%02d" , c.get(Calendar.MINUTE))+":"+
				String.format("%02d" , c.get(Calendar.SECOND))+":"+
				String.format("%03d" , c.get(Calendar.MILLISECOND));
		String[] str=time.split(":");
		int timeToCheck=Integer.parseInt(str[0]);
		if(timeToCheck>=23){
			timeToCheck=-1;
		}
		if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=22)&&(dayLongName.equalsIgnoreCase("Monday")||dayLongName.equalsIgnoreCase("Tuesday")
				||dayLongName.equalsIgnoreCase("Wednesday")||dayLongName.equalsIgnoreCase("Thursday")||dayLongName.equalsIgnoreCase("Friday"))/*&&delivery_type.toLowerCase().equalsIgnoreCase("asap")*/){

			return true;

		}/*else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=23)&&(dayLongName.equalsIgnoreCase("Saturday"))*//*&&delivery_type.toLowerCase().equalsIgnoreCase("asap")*//*){

			*//*if((Integer.parseInt(str[0])<9)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=16)&&(Integer.parseInt(str[1])>=30)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=17)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else{
				return false;
			}*//*
			return  true;

		}*/else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=20)&&(dayLongName.equalsIgnoreCase("Sunday")||dayLongName.equalsIgnoreCase("Saturday"))/*&&delivery_type.toLowerCase().equalsIgnoreCase("asap")*/){

			return true;

		}
		else{
			return  false;
		}
	}

	public static boolean checkWorkingHour(ArrayList<String> delivery_type){
		TimeZone tz = TimeZone.getTimeZone("GMT+02:00");
		Calendar c = Calendar.getInstance(tz);
		String dayLongName = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		String time = String.format("%02d" , c.get(Calendar.HOUR_OF_DAY))+":"+
				String.format("%02d" , c.get(Calendar.MINUTE))+":"+
				String.format("%02d" , c.get(Calendar.SECOND))+":"+
				String.format("%03d" , c.get(Calendar.MILLISECOND));
		String[] str=time.split(":");
		int timeToCheck=Integer.parseInt(str[0]);
		if(timeToCheck>=23){
			timeToCheck=-1;
		}
		if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=22)&&(dayLongName.equalsIgnoreCase("Monday")||dayLongName.equalsIgnoreCase("Tuesday")
				||dayLongName.equalsIgnoreCase("Wednesday")||dayLongName.equalsIgnoreCase("Thursday")||dayLongName.equalsIgnoreCase("Friday"))/*&&!delivery_type.toString().contains("NO RUSH")*/){

			return true;

		}/*else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=23)&&(dayLongName.equalsIgnoreCase("Saturday"))*//*&&!delivery_type.toString().contains("NO RUSH")*//*){

			*//*if((Integer.parseInt(str[0])<9)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=16)&&(Integer.parseInt(str[1])>=30)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else if((Integer.parseInt(str[0])>=17)&&(dayLongName.equalsIgnoreCase("Saturday"))){
				return true;
			}else{
				return false;
			}*//*
			return  true;

		}*/else if((Integer.parseInt(str[0])<8||Integer.parseInt(str[0])>=20)&&(dayLongName.equalsIgnoreCase("Sunday")||dayLongName.equalsIgnoreCase("Saturday"))/*&&!delivery_type.toString().contains("NO RUSH")*/){

			return true;

		}

		else{
			return  false;
		}
	}

	public void RateApp(){
		Uri uri = Uri.parse("market://details?id=" + mContext.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
				Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
				Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
		try {
			mContext.startActivity(goToMarket);
		} catch (ActivityNotFoundException e) {
			mContext.startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://play.google.com/store/apps/details?id=" + mContext.getPackageName())));
		}
	}

	public void getDialogRateUs() {
		final Dialog dialog = new Dialog(mContext);
		Window window = dialog.getWindow();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		dialog.setContentView(R.layout.dialog_rate_app);
		dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		Button btn_yes=(Button)dialog.findViewById(R.id.btn_yes);
		Button btn_no=(Button)dialog.findViewById(R.id.btn_no);
		Button btn_later=(Button)dialog.findViewById(R.id.btn_later);
		btn_yes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				RateApp();
			}
		});
		btn_no.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		btn_later.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
	}

	public void shareUrl(String strCustName){
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, "I am using StockupAPP. Download it now at www.stockup.co.za.");
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		mContext.startActivity(shareIntent);

	}

	public static String getIPAddress(boolean useIPv4) {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress();
						//boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						boolean isIPv4 = sAddr.indexOf(':')<0;

						if (useIPv4) {
							if (isIPv4)
								return sAddr;
						} else {
							if (!isIPv4) {
								int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
								return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
							}
						}
					}
				}
			}
		} catch (Exception ex) { } // for now eat exceptions
		return "";
	}
	public String getAndroidVersion() {
		String release = Build.VERSION.RELEASE;
		int sdkVersion = Build.VERSION.SDK_INT;
		return "Android SDK: " + sdkVersion + " (" + release +")";
	}

	public boolean cardExpiryDate(String day,String month, String year){
		String input = day+"/"+month+"/"+year; // for example
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		simpleDateFormat.setLenient(false);
		Date expiry = null;
		try {
			expiry = simpleDateFormat.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		boolean expired = expiry.before(new Date());
		return expired;
	}

	public boolean cardExpiryDate(String month, String year){
		Calendar c = Calendar.getInstance();
		int yr = c.get(Calendar.YEAR);
		int mon = c.get(Calendar.MONTH)+1;

		int incomingYr = Integer.parseInt(year);
		int incomingMon = Integer.parseInt(month);

		boolean retVal = false;
		if (incomingYr > yr){
			retVal = true;
		}else if (incomingYr == yr && incomingMon >= mon){
			retVal = true;
		}else {
			retVal = false;
		}
		//Toast.makeText(geta, String.valueOf(retVal), Toast.LENGTH_LONG).show();
		return retVal;
	}

	public void getVersionCode(){

	}
}

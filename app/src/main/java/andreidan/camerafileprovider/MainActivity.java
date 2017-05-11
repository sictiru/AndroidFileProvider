package andreidan.camerafileprovider;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    static final int RC_PERM_READ_EXTERNAL_STORAGE = 1;
    static final int RC_PICK_PHOTO_GALLERY = 2;
    static final int RC_PICK_PHOTO_CAMERA = 3;

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.button_camera)
    Button btnCamera;
    @BindView(R.id.button_gallery)
    Button btnGalley;

    private String cameraFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        btnCamera.setOnClickListener(v -> openCamera(getLocalImageFileName()));
        btnGalley.setOnClickListener(v -> openGallery());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraPermissionResult(requestCode, grantResults, cameraFile);
    }

    private void cameraPermissionResult(int requestCode, int[] grantResults, String fileName) {
        if (PermissionsUtils.permissionGranted(requestCode, RC_PERM_READ_EXTERNAL_STORAGE, grantResults)) {
            openCamera(fileName);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_PICK_PHOTO_GALLERY);
    }

    private void openCamera(String fileName) {
        if (PermissionsUtils.requestPermission(this, RC_PERM_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File cameraOutput = getCameraOutput(fileName);
            Uri outPut = getUriFromFile(cameraOutput);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outPut);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, RC_PICK_PHOTO_CAMERA);
        }
    }

    private Uri getUriFromFile(File cameraOutput) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", cameraOutput);
        } else {
            return Uri.fromFile(cameraOutput);
        }
    }

    public File getCameraOutput(String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Invalid file name", Toast.LENGTH_SHORT).show();
        }

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, fileName);
    }

    public String getLocalImageFileName() {
        return cameraFile = "test-" + UUID.randomUUID().toString() + ".jpeg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_PICK_PHOTO_CAMERA:
                if (resultCode == Activity.RESULT_OK) {
                    File cameraOutput = getCameraOutput(cameraFile);
                    if (cameraOutput != null) {
                        Uri selectedImageUri = Uri.fromFile(cameraOutput);
                        imageView.setImageURI(selectedImageUri);
                    }
                }
                break;
            case RC_PICK_PHOTO_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImageUri = data.getData();
                    imageView.setImageURI(selectedImageUri);
                }
                break;
        }
    }
}

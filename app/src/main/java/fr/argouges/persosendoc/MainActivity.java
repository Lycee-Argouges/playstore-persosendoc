package fr.argouges.persosendoc;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_ACCOUNT_PICKER = 1;
    static final int REQUEST_AUTHORIZATION = 2;

    static String FILE_TITLE = "google_drive_test";

    private Drive service = null;
    private GoogleAccountCredential credential = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText nomPC = (EditText) findViewById(R.id.toolbar_title);
        nomPC.append("NetBIOS ?");
        /*CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_title);
        toolBarLayout.setTitle(getTitle());*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (service == null) {
            credential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        }

        ((Button)findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    showToast("Enregistrement");
                    saveTextToDrive();
                } else {
                    showToast("Vous devez vous connecter !");
                }
            }
        });
        ((Button)findViewById(R.id.loadButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (service != null) {
                    showToast("Chargement");
                    loadTextFromDrive();
                    moveTextToDrive();
                } else {
                    showToast("Vous devez vous connecter !");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        service = getDriveService(credential);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    saveTextToDrive();
                } else {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
        }
    }

    /*private void saveTextToDrive() {
        final String inputText = ((EditText)findViewById(R.id.editText)).getText().toString();
        final String editTextsave = ((EditText)findViewById(R.id.toolbar_title)).getText().toString();
        FILE_TITLE = editTextsave;
        Thread t = new Thread(new Runnable() {
            //@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void run() {
                try {

                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            fileIdOrNull = f.getId();
                        }
                    }

                    File body = new File();
                    body.setName(FILE_TITLE);//fileContent.getName());
                    body.setMimeType("text/plain");

                    //ByteArrayContent content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));
                    ByteArrayContent content = null;
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {}
                        content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));
                    //
                    if (fileIdOrNull == null) {
                        service.files().create(body, content).execute();
                        showToast("Ajouté !");

                    } else {
                        service.files().update(fileIdOrNull, body, content).execute();
                        showToast("Modifié !");
                    }
                    // TODO 失敗時の処理?
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Une erreur est survenue...");
                    e.printStackTrace();
                }
                try {
                    String fileId = null;
                    FileList list = service.files().list().execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            fileId = f.getId();
                        }
                    }
                    String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                    // Retrieve the existing parents to remove
                    File file = service.files().get(fileId)
                            .setFields("parents")
                            .execute();
                    StringBuilder previousParents = new StringBuilder();
                    for (String parent : file.getParents()) {
                        previousParents.append(parent);
                        previousParents.append(',');
                    }
                    // Move the file to the new folder
                    file = service.files().update(fileId, null)
                            .setAddParents(folderId)
                            .setRemoveParents(previousParents.toString())
                            .setFields("id, parents")
                            .execute();
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Erreur dans l'indexation...");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }*/

    private void saveTextToDrive() {
        final String inputText = ((EditText)findViewById(R.id.editText)).getText().toString();
        final String editTextsave = ((EditText)findViewById(R.id.toolbar_title)).getText().toString();
        FILE_TITLE = editTextsave;
        Thread t = new Thread(new Runnable() {
            //@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void run() {
                String fileIdMove = null;
                File body = new File();
                body.setName(FILE_TITLE);//fileContent.getName());
                body.setMimeType("text/plain");
                //ByteArrayContent content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));
                ByteArrayContent content = null;
                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {}
                content = new ByteArrayContent("text/plain", inputText.getBytes(Charset.forName("UTF-8")));
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    Boolean SearchTrue = false;
                    FileList list = service.files().list().setQ("mimeType='text/plain'").execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            SearchTrue = true;
                            String fileId = null;
                            fileId = f.getId();
                            // Retrieve the existing parents to remove
                            File file = service.files().get(fileId)
                                    .setFields("parents")
                                    .execute();
                            String MyParent = file.getParents().get(0).toString();
                            String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                            if (MyParent.equals(folderId)) {
                                service.files().update(fileId, body, content).execute();
                                showToast("Modifié !");
                                //fileIdOrNull = f.getId();
                                fileIdMove = fileId ;
                            }
                        }
                    }
                    if(fileIdMove == null) {
                        service.files().create(body, content).execute();
                        showToast("Ajouté !");
                        FileList list2 = service.files().list().setQ("mimeType='text/plain'").execute();
                        for (File f : list2.getFiles()) {
                            if (FILE_TITLE.equals(f.getName())) {
                                //fileIdMove = f.getId();
                                String fileId = null;
                                fileId = f.getId();
                                // Retrieve the existing parents to remove
                                File file = service.files().get(fileId)
                                        .setFields("parents")
                                        .execute();
                                String MyParent = file.getParents().get(0).toString();
                                //showToast(MyParent);
                                //content = new ByteArrayContent("text/plain", MyParent.getBytes(Charset.forName("UTF-8")));
                                //service.files().update(fileId, body, content).execute();
                                String folderIdRoot = "0AA3WkOvmQP6RUk9PVA";
                                String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                                //final TextView helloTextView = (TextView) findViewById(R.id.editText);
                                //helloTextView.setText(MyParent);
                                if (MyParent.equals(folderId) || MyParent.equals(folderIdRoot)) {
                                    fileIdMove = fileId ;
                                }
                            }
                        }
                    }

                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Une erreur est survenue...");
                    e.printStackTrace();
                }
                if (fileIdMove != null) {
                    try {
                        // TODO 失敗時の処理?
                        String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                        // Retrieve the existing parents to remove
                        File file = service.files().get(fileIdMove)
                                .setFields("parents")
                                .execute();
                        StringBuilder previousParents = new StringBuilder();
                        for (String parent : file.getParents()) {
                            previousParents.append(parent);
                            previousParents.append(',');
                        }
                        // Move the file to the new folder
                        file = service.files().update(fileIdMove, null)
                                .setAddParents(folderId)
                                .setRemoveParents(previousParents.toString())
                                .setFields("id, parents")
                                .execute();
                    } catch (UserRecoverableAuthIOException e) {
                        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    } catch (IOException e) {
                        showToast("Erreur dans l'indexation...");
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    private void loadTextFromDrive() {
        final String editTextload = ((EditText)findViewById(R.id.toolbar_title)).getText().toString();
        FILE_TITLE = editTextload;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 指定のタイトルのファイルの ID を取得
                    String fileIdOrNull = null;
                    String fileId = null;
                    String NameFiles = null;
                    //FileList list = service.files().list().execute();
                    FileList list = service.files().list().setQ("mimeType='text/plain'").execute();
                    for (File f : list.getFiles()) {
                        NameFiles = NameFiles + " - " + f.getName();
                        if (FILE_TITLE.equals(f.getName())) {
                            fileId = f.getId();
                            String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                            // Retrieve the existing parents to remove
                            File file = service.files().get(fileId)
                                    .setFields("parents")
                                    .execute();
                            String MyParent = file.getParents().get(0).toString();
                            if (MyParent.equals(folderId)) {
                                //fileIdOrNull = f.getId();
                                fileIdOrNull = fileId ;
                                showToast("Downloading...");
                            }
                        }
                    }
                    /*String finalNameFiles = NameFiles;
                    runOnUiThread(new Runnable() {
                        @Override public void run() {
                            final TextView helloTextView = (TextView) findViewById(R.id.editText);
                            //helloTextView.setText(MyParent);
                            helloTextView.setText(finalNameFiles);
                        }
                    });*/

                    if (fileIdOrNull != null) {
                        final String text = downloadFile(service, fileIdOrNull);
                        runOnUiThread(new Runnable() {
                            @Override public void run() {
                                ((EditText)findViewById(R.id.editText)).setText(text);
                            }
                        });
                    } else {
                        showToast("File not found...");
                    }
                    // TODO 失敗時の処理?
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Une erreur est survenue...");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private void moveTextToDrive() {
        final String editTextmove = ((EditText)findViewById(R.id.toolbar_title)).getText().toString();
        FILE_TITLE = editTextmove;
        Thread t = new Thread(new Runnable() {
            //@RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void run() {
                try {
                    String fileId = null;
                    FileList list = service.files().list().setQ("mimeType='text/plain'").execute();
                    for (File f : list.getFiles()) {
                        if (FILE_TITLE.equals(f.getName())) {
                            fileId = f.getId();
                            String folderId = "1TaO-Hb-iJ9wxg3Oj2O3sbKE7UxxlJ0S_";
                            // Retrieve the existing parents to remove
                            File file = service.files().get(fileId)
                                    .setFields("parents")
                                    .execute();
                            String MyParent = file.getParents().get(0).toString();
                            if (MyParent.equals(folderId)) {
                                StringBuilder previousParents = new StringBuilder();
                                for (String parent : file.getParents()) {
                                    previousParents.append(parent);
                                    previousParents.append(',');
                                }
                                // Move the file to the new folder
                                file = service.files().update(fileId, null)
                                        .setAddParents(folderId)
                                        .setRemoveParents(previousParents.toString())
                                        .setFields("id, parents")
                                        .execute();
                            }
                        }
                    }
                } catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                } catch (IOException e) {
                    showToast("Erreur dans l'indexation...");
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    /**
     * @see <a href="https://developers.google.com/drive/v3/web/manage-downloads">Download Files | Drive REST API</a>
     */
    private static String downloadFile(Drive service, String fileId) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            service.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            //service.files().export(fileId, "text/plain").executeMediaAndDownloadTo(outputStream);
            return outputStream.toString("UTF-8");
        } catch (IOException e) {
            // An error occurred.
            e.printStackTrace();
            //return null;
            return "Erreur!";
        }
    }

    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
                .build();
    }

    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

}

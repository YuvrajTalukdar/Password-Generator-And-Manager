/**
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yuvraj.passwordgeneratorandmanager;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    public Task<Boolean> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {
            try {
                OutputStream outputStream = new FileOutputStream(targetFile);
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return true;
            }
            catch(Exception e)
            {   return false;}
        });
    }

    public Task<Void> delete_backup_file()
    {
        return Tasks.call(mExecutor,()->{
            try {
                List<File> files=mDriveService.files().list().setSpaces("drive").execute().getFiles();
                System.out.println("Size1===="+files.size());
                for(int a=0;a<files.size();a++)
                {
                    if(files.get(a).getName().contains("PasswordGeneratorAndManagerBackup"))
                    {
                        System.out.println("Delete attempt "+a);
                        mDriveService.files().delete(files.get(a).getId()).execute();
                        break;
                    }
                }
            }
            catch(Exception e)
            {   e.printStackTrace();}
            return null;
        });
    }

    public Task<Boolean> uploadFile(String name, java.io.File java_zip_file) {
        return Tasks.call(mExecutor, () -> {
            try {
                File metadata = new File().setParents(Collections.singletonList("root")).setMimeType("").setName(name);
                FileContent fileContent = new FileContent("", java_zip_file);
                mDriveService.files().create(metadata, fileContent).execute();
                //delete the zip file
                java_zip_file.delete();
                return true;
            }
            catch(Exception e)
            {   e.printStackTrace();return false;}
            });
    }

    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService.files().list().setSpaces("drive").execute());
    }
}

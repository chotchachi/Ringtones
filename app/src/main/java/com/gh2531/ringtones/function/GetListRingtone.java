package com.gh2531.ringtones.function;

import android.support.annotation.NonNull;
import android.util.Log;

import com.gh2531.ringtones.adapter.RingtoneAdapter;
import com.gh2531.ringtones.model.Ringtone;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GetListRingtone {
    public FirebaseFirestore firebaseFirestore;

    public void GetListRingtone(String PATH_COLLECTION_RINGTONE_FIREBASE,
                                String PATH_DOCUMENT_RINGTONE_FIREBASE,
                                String PATH_DATA_RINGTONE_FIREBASE,
                                final ArrayList<Ringtone> arrayList,
                                final RingtoneAdapter adapter)
    {
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection(PATH_COLLECTION_RINGTONE_FIREBASE)
                .document(PATH_DOCUMENT_RINGTONE_FIREBASE)
                .collection(PATH_DATA_RINGTONE_FIREBASE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Ringtone ringtone = document.toObject(Ringtone.class);
                                arrayList.add(ringtone);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}

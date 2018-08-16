package com.example.android.instalineenterprise;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.support.v4.app.ListFragment;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseUser;
import com.mcxtzhang.commonadapter.lvgv.CommonAdapter;
import com.mcxtzhang.commonadapter.lvgv.ViewHolder;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.List;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class WaitingListFragment extends ListFragment {

    public StorageReference storageReference;
    public DatabaseReference databaseReference;
    public FirebaseAuth firebaseAuth;


    private Map<User, Integer> usersMap;


    public WaitingListFragment() {
        // Required empty public constructor
    }

    private ListView mLv;

    private CustomAdapter adapter;
    ;

    List<User> NAME;
    List<String>RANK;

    public int counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new CustomAdapter(usersMap);
        NAME = new ArrayList<User>();
        RANK = new ArrayList<String>();


        System.out.println("===================== ON CREATE ");



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);
        System.out.println("===================== ON CREATE VIEW ");

        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        // you can add listener of elements here
          /*Button mButton = (Button) view.findViewById(R.id.button);
            mButton.setOnClickListener(this); */

        mLv = view.findViewById(R.id.myListView);
        System.out.println("===================== ON VIEW CREATED ");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get database
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        final String enterpriseID = firebaseAuth.getCurrentUser().getUid();
        final DatabaseReference restDict = databaseReference.child("Restaurant");
        final DatabaseReference chosenRest = restDict.child(enterpriseID);
        final DatabaseReference userList = chosenRest.child("UserQueue");
        final DatabaseReference userRef = databaseReference.child("Users");
//        final DatabaseReference Users = chosenRest.child("Users");

        usersMap = new HashMap<User, Integer>();

        userList.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String waitingUser = dataSnapshot.getKey();
                if (dataSnapshot.child("queueNumber").getValue(Integer.class) != null) {
                    final int queueNumber = dataSnapshot.child("queueNumber").getValue(Integer.class);
                    DatabaseReference curUserReference = userRef.child(waitingUser);
                    curUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String nickName = dataSnapshot.child("username").getValue(String.class);
                            String profilePath =  dataSnapshot.child("profileImage").getValue(String.class);
                            User user = new User(nickName, profilePath, queueNumber);
                            usersMap.put(user, queueNumber);
                            NAME.add(user);
                            RANK.add(String.valueOf(queueNumber));
                            adapter.notifyDataSetChanged();
                            System.out.println("***************************** hi");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        mLv.setAdapter(adapter);
        System.out.println("===================== ON ACTIVITY CREATED ");
        System.out.println(adapter.getCount());
    }



    class CustomAdapter extends BaseAdapter{

        private Map<User, Integer> usersmap;

        public CustomAdapter(Map<User, Integer> userMap){
            usersmap = userMap;
        }

        @Override
        public int getCount() {
            return NAME.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            System.out.println("===================== GET VIEW ");
            System.out.println(usersMap);

            convertView = getLayoutInflater().inflate(R.layout.item_cst_swipe, null);
            CircleImageView imageView = (CircleImageView) convertView.findViewById(R.id.waitUserImage);
            TextView userName = (TextView) convertView.findViewById(R.id.waitUserName);
            TextView rank = (TextView) convertView.findViewById(R.id.queueRank);

            System.out.println(NAME);
            System.out.println(RANK);

            if(NAME.size() != 0){
                userName.setText(NAME.get(position).getName());
                rank.setText(RANK.get(position));
                Glide.with(getContext()).load(NAME.get(position).getProfileImage()).into(imageView);
            }

            Button deleteBtn = (Button) convertView.findViewById(R.id.btnDelete);

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firebaseAuth = FirebaseAuth.getInstance();
                    storageReference = FirebaseStorage.getInstance().getReference();
                    databaseReference = FirebaseDatabase.getInstance().getReference();

                    final String enterpriseID = firebaseAuth.getCurrentUser().getUid();
                    final DatabaseReference restDict = databaseReference.child("Restaurant");
                    final DatabaseReference chosenRest = restDict.child(enterpriseID);
                    final DatabaseReference userList = databaseReference.child("Users");
                    final String username = NAME.get(position).getName();

                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> iterable = dataSnapshot.child("Restaurant").child(enterpriseID).child("UserQueue").getChildren();
                            Iterator iterator = iterable.iterator();
                            int lastNumber = 0;
                            String userID = "abc";

                            while (iterator.hasNext()){
                                DataSnapshot temp = (DataSnapshot) iterator.next();
                                userID = temp.getKey();
                                if (!userID.equals("placeholder")) {

                                    String nickName = temp.child("nickName").getValue(String.class);

                                    if (nickName.equals(username)) {

                                        userID = temp.getKey();

                                        lastNumber = dataSnapshot.child("Users").child(userID).child("Wait").child(enterpriseID).child("lineNumber").getValue(Integer.class);
                                        chosenRest.child("UserQueue").child(userID).removeValue();
                                        userList.child(userID).child("Wait").child(enterpriseID).removeValue();

                                        int queueNumber = dataSnapshot.child("Restaurant").child(enterpriseID).child("queueNumber").getValue(Integer.class);
                                        chosenRest.child("queueNumber").setValue(queueNumber - 1);
                                        break;

                                    }
                                }
                            }


                            for (DataSnapshot datasnapshot : dataSnapshot.child("Restaurant").child(enterpriseID).child("UserQueue").getChildren()){
                                String userid = datasnapshot.getKey();
                                if (!userid.equals("placeholder") && !userid.equals(userID)){
                                    System.out.println("shitshit");
                                    int currentUserTime = dataSnapshot.child("Users").child(userid).child("Wait").child(enterpriseID).child("lineNumber").getValue(Integer.class);
                                    if(currentUserTime > lastNumber){
                                        userList.child(userid).child("Wait").child(enterpriseID).child("lineNumber").setValue(currentUserTime - 1);
                                    }
                                    //chosenRest.child("UserQueue").child(userid).child("queueNumber").setValue(currentUserTime - lastUserNumber);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    NAME.remove(position);
                    RANK.remove(position);
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

}
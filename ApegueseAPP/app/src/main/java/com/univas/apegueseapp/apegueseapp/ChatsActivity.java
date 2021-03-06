package com.univas.apegueseapp.apegueseapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.univas.apegueseapp.apegueseapp.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatsActivity extends AppCompatActivity {


    private RecyclerView myChatList;
    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");



        myChatList = (RecyclerView) findViewById(R.id.chat_list);
        myChatList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myChatList.setLayoutManager(linearLayoutManager);

        DisplayAllChats();

    }


    // listar todos os Chats do usuario
    private void DisplayAllChats() {
        FirebaseRecyclerAdapter<Friends, ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, ChatsViewHolder>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        ChatsViewHolder.class,
                        ChatsRef
                ) {
            @Override
            protected void populateViewHolder(final ChatsViewHolder viewHolder, Friends model, final int position) {





                final String usersIDs = getRef(position).getKey();

                getRef(position).addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if(dataSnapshot.exists()){
                            long number = dataSnapshot.getChildrenCount();
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            List<Messages> messagesList = new ArrayList<>();
                            messagesList.add(messages);
                            int size =   messagesList.size()-1;
                            viewHolder.setLastMenssage(messagesList.get(size).getMessage().toString());
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
                //pega o id de cada amigo
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            final String userName = dataSnapshot.child("fullname").getValue().toString();
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString();
                            final String type;

                            //verificar se userSstate existe
                            if(dataSnapshot.hasChild("userState")){
                                type = dataSnapshot.child("userState").child("type").getValue().toString(); //recebe status(online/offline)
                                if(type.equals("online")){
                                    viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                }else{
                                    viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            viewHolder.setFullname(userName);
                            viewHolder.setProfileimage(getApplicationContext(), profileImage);
                            //alert dialog
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                                Intent chatintent = new Intent(ChatsActivity.this, ChatActivity.class);
                                                chatintent.putExtra("visit_user_id", usersIDs);
                                                chatintent.putExtra("username", userName);
                                                startActivity(chatintent);
                                            }
                                        });

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        myChatList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class ChatsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView onlineStatusView;
        public ChatsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);

        }
        public void setFullname(String fullname){
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setLastMenssage(String menssage){
            TextView lastMenssage = (TextView) mView.findViewById(R.id.all_users_status);
            lastMenssage.setText(menssage);
        }
    }




}

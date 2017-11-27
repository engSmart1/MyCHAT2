package musictag.hytham1.com.mychat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import model.Messages;

/**
 * Created by Hytham on 11/23/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.messageViewHolder>
{
    private List<Messages> userMessageList ;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }

    @Override
    public messageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View V = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout_of_users , parent ,false);

        mAuth = FirebaseAuth.getInstance();
        return new messageViewHolder(V);
    }

    @Override
    public void onBindViewHolder(messageViewHolder holder, int position)
    {
        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);
        String fromUserId = messages.getFrom();

        if (fromUserId.equals(message_sender_id))
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_two);
            holder.messageText.setTextColor(Color.BLACK);
            holder.messageText.setGravity(Gravity.RIGHT);
        }
        else
        {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);
            holder.messageText.setGravity(Gravity.LEFT);
        }

        holder.messageText.setText(messages.getMessage());

    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    public class messageViewHolder extends RecyclerView.ViewHolder
    {
         public TextView messageText;
         public CircleImageView userProfileImage;

        public messageViewHolder(View view) {
            super(view);

            messageText = view.findViewById(R.id.message_text);
          //  userProfileImage = view.findViewById(R.id.messages_profile_images);
        }
    }
}

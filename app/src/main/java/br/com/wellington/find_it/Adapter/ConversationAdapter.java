package br.com.wellington.find_it.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.wellington.find_it.Bean.ChatConversation;
import br.com.wellington.find_it.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Classe Adaptadora de Listagem de Conversas
 *
 * @author Wellington
 * @version 1.0 - 03/01/2017.
 */
public class ConversationAdapter extends BaseAdapter {

    private final List<ChatConversation> chatConversations;
    private Activity context;

    public ConversationAdapter(Activity context, List<ChatConversation> chatConversations) {
        this.context = context;
        this.chatConversations = chatConversations;
    }

    @Override
    public int getCount() {
        if (chatConversations != null) {
            return chatConversations.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatConversation getItem(int position) {
        if (chatConversations != null) {
            return chatConversations.get(position);
        } else {
            return null;
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String formatTime(Date lastMessage) {
        Calendar cal = Calendar.getInstance();
        Date c_date = cal.getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        if (fmt.format(c_date).equals(fmt.format(lastMessage))) {
            return new SimpleDateFormat("HH:mm").format(lastMessage);
        } else {
            cal.add(Calendar.DAY_OF_YEAR, -1);
            c_date = cal.getTime();
            if (fmt.format(c_date).equals(fmt.format(lastMessage))) {
                return "ONTEM";
            } else {
                return new SimpleDateFormat("dd/MM/yyyy").format(lastMessage);
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"SimpleDateFormat", "ViewHolder"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatConversation chatConversation = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = vi.inflate(R.layout.list_conversation, null);
        holder = createViewHolder(convertView);
        convertView.setTag(holder);

        holder.lastMessageConversation.setText(chatConversation.getUltimaMensagem());
        holder.nameConversation.setText(chatConversation.getNomeCliente());
        try {
            holder.timeConversation.setText(formatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(chatConversation.getDataUltimaMensagem())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (chatConversation.getNovasMensagens() > 0) {
            holder.unreadCountConversation.setText(String.valueOf(chatConversation.getNovasMensagens()));
            holder.timeConversation.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        } else {
            holder.unreadCountConversation.setVisibility(View.GONE);
            holder.timeConversation.setTextColor(ContextCompat.getColor(context, R.color.textColorMediumDarkGrey));
        }

        if (chatConversation.getLinkFotoCliente() != null && !chatConversation.getLinkFotoCliente().equals(""))
            Picasso.with(context).load(chatConversation.getLinkFotoCliente()).noFade().placeholder(R.drawable.profile).into(holder.imageConversation);

        return convertView;
    }

    public void add(ChatConversation message) {
        chatConversations.add(message);
    }

    public void add(List<ChatConversation> conversations) {
        chatConversations.addAll(conversations);
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.lastMessageConversation = (TextView) v.findViewById(R.id.last_message_conversation);
        holder.imageConversation = (CircleImageView) v.findViewById(R.id.image_conversation);
        holder.nameConversation = (TextView) v.findViewById(R.id.name_conversation);
        holder.timeConversation = (TextView) v.findViewById(R.id.time_conversation);
        holder.unreadCountConversation = (TextView) v.findViewById(R.id.unread_count_conversation);
        return holder;
    }


    private static class ViewHolder {
        private TextView lastMessageConversation;
        private CircleImageView imageConversation;
        private TextView nameConversation;
        private TextView timeConversation;
        private TextView unreadCountConversation;
    }
}
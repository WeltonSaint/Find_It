package br.com.wellington.find_it.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TreeSet;

import br.com.wellington.find_it.Bean.ChatMessage;
import br.com.wellington.find_it.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Classe Adaptadora de Listagem de Mensagens do Chat
 *
 * @author Wellington
 * @version 1.0 - 02/01/2017.
 */
public class ChatAdapter extends BaseAdapter {

    private final List<ChatMessage> chatMessages;
    private TreeSet<Integer> sectionHeader = new TreeSet<>();
    private Activity context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    public ChatAdapter(Activity context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    public void addItem(final ChatMessage item) {
        chatMessages.add(item);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final ChatMessage item) {
        chatMessages.add(item);
        sectionHeader.add(chatMessages.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "SimpleDateFormat"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        boolean myMsg = chatMessage.getIsme();//Just a dummy check to simulate whether it me or other sender
        boolean lastMsg;
        int rowType = getItemViewType(position);
        if (rowType == TYPE_ITEM) {
            if (myMsg)
                convertView = vi.inflate(R.layout.message_out, null);
            else
                convertView = vi.inflate(R.layout.message_in, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
            lastMsg = getItemViewType(position - 1) != TYPE_SEPARATOR && getItem(position - 1).getIsme() == myMsg;

            setAlignment(holder, myMsg, lastMsg);
            holder.txtMessage.setText(chatMessage.getMessage());
            if (chatMessage.getLinkFotoPerfil() != null && !chatMessage.getLinkFotoPerfil().equals(""))
                Picasso.with(context).load(chatMessage.getLinkFotoPerfil()).noFade().placeholder(R.drawable.profile).into(holder.profileImage);

            try {
                holder.txtInfo.setText(new SimpleDateFormat("HH:mm").format(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(chatMessage.getDate())));
            } catch (ParseException e) {
                e.printStackTrace();
                holder.txtInfo.setText(chatMessage.getDate());
            }
        } else {
            convertView = vi.inflate(R.layout.header_date, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
            holder.txtInfo.setText(chatMessage.getDate());
        }
        return convertView;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    public void add(List<ChatMessage> messages) {
        chatMessages.addAll(messages);
    }

    private void setAlignment(ViewHolder holder, boolean isMe, boolean lastMsg) {
        Drawable icon;
        if (!isMe) {
            if (!lastMsg) {
                icon = ContextCompat.getDrawable(context, R.drawable.incoming_message_bg).mutate();
                holder.profileImage.setVisibility(View.VISIBLE);

            } else {
                icon = ContextCompat.getDrawable(context, R.drawable.incoming_extra_message_bg).mutate();
                holder.profileImage.setVisibility(View.INVISIBLE);
            }
        } else {
            if (!lastMsg) {
                icon = ContextCompat.getDrawable(context, R.drawable.outgoing_message_bg).mutate();
                holder.profileImage.setVisibility(View.VISIBLE);
            } else {
                icon = ContextCompat.getDrawable(context, R.drawable.outgoing_extra_message_bg).mutate();
                holder.profileImage.setVisibility(View.INVISIBLE);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.contentWithBG.setBackground(icon);
        }
    }


    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.profileImage = (CircleImageView) v.findViewById(R.id.profileImage);
        holder.contentWithBG = (RelativeLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        return holder;
    }


    private static class ViewHolder {
        private TextView txtMessage;
        private CircleImageView profileImage;
        private TextView txtInfo;
        private RelativeLayout contentWithBG;
    }
}

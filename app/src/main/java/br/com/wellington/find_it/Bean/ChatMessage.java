package br.com.wellington.find_it.Bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Spannable;

/**
 * Classe de Mensagem do Chat
 *
 * @author Wellington
 * @version 1.0 - 02/01/2017.
 */
public class ChatMessage implements Parcelable {
    private long userId;
    private String name;
    private boolean isMe;
    private Spannable message;
    private String linkFotoPerfil;
    private String dateTime;

    public ChatMessage() {
    }

    private ChatMessage(Parcel in) {
        userId = in.readLong();
        name = in.readString();
        isMe = in.readByte() != 0;
        linkFotoPerfil = in.readString();
        dateTime = in.readString();
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public long getId() {
        return userId;
    }

    public void setId(long id) {
        this.userId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsme() {
        return isMe;
    }

    public void setMe(boolean isMe) {
        this.isMe = isMe;
    }

    public Spannable getMessage() {
        return message;
    }

    public void setMessage(Spannable message) {
        this.message = message;
    }

    public String getLinkFotoPerfil() {
        return linkFotoPerfil;
    }

    public void setLinkFotoPerfil(String linkFotoPerfil) {
        this.linkFotoPerfil = linkFotoPerfil;
    }

    public String getDate() {
        return dateTime;
    }

    public void setDate(String dateTime) {
        this.dateTime = dateTime;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(userId);
        parcel.writeString(name);
        parcel.writeByte((byte) (isMe ? 1 : 0));
        parcel.writeString(linkFotoPerfil);
        parcel.writeString(dateTime);
    }
}

package br.com.wellington.find_it.Bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Classe de Cliente
 *
 * @author Wellington
 * @version 1.0 - 07/01/2017.
 */
public class Cliente implements Serializable, Parcelable {

    private long codigoCliente;
    private String nomeCliente;
    private String emailCliente;
    private String senhaCliente;
    private String contatoCliente;
    private String linkFotoPerfilCliente;

    public Cliente(long codigoCliente, String nomeCliente, String emailCliente, String senhaCliente, String contatoCliente) {
        this.codigoCliente = codigoCliente;
        this.nomeCliente = nomeCliente;
        this.emailCliente = emailCliente;
        this.senhaCliente = senhaCliente;
        this.contatoCliente = contatoCliente;
    }

    private Cliente(Parcel in) {
        codigoCliente = in.readLong();
        nomeCliente = in.readString();
        emailCliente = in.readString();
        senhaCliente = in.readString();
        contatoCliente = in.readString();
        linkFotoPerfilCliente = in.readString();
    }

    public static final Creator<Cliente> CREATOR = new Creator<Cliente>() {
        @Override
        public Cliente createFromParcel(Parcel in) {
            return new Cliente(in);
        }

        @Override
        public Cliente[] newArray(int size) {
            return new Cliente[size];
        }
    };

    public long getCodigoCliente() {
        return codigoCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getEmailCliente() {
        return emailCliente;
    }

    public String getSenhaCliente() {
        return senhaCliente;
    }

    public String getContatoCliente() {
        return contatoCliente;
    }

    public String getLinkFotoPerfilCliente() {
        return linkFotoPerfilCliente;
    }

    public void setLinkFotoPerfilCliente(String linkFotoPerfilCliente) {
        this.linkFotoPerfilCliente = linkFotoPerfilCliente;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(codigoCliente);
        parcel.writeString(nomeCliente);
        parcel.writeString(emailCliente);
        parcel.writeString(senhaCliente);
        parcel.writeString(contatoCliente);
        parcel.writeString(linkFotoPerfilCliente);
    }
}

package br.com.wellington.find_it.Bean;

import java.io.Serializable;

/**
 * Classe de Conversa do Chat
 *
 * @author Wellington
 * @version 1.0 - 03/01/2017.
 */
public class ChatConversation implements Serializable{

    private long meuId;
    private String linkMinhaFoto;
    private long idCliente;
    private String nomeCliente;
    private String linkFotoCliente;
    private String ultimaMensagem;
    private int novasMensagens;
    private boolean online;
    private String dataUltimaMensagem;

    public ChatConversation() {
    }

    public long getMeuId() {
        return meuId;
    }

    public void setMeuId(long meuId) {
        this.meuId = meuId;
    }

    public String getLinkMinhaFoto() {
        return linkMinhaFoto;
    }

    public void setLinkMinhaFoto(String linkMinhaFoto) {
        this.linkMinhaFoto = linkMinhaFoto;
    }

    public long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(long idCliente) {
        this.idCliente = idCliente;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public String getLinkFotoCliente() {
        return linkFotoCliente;
    }

    public void setLinkFotoCliente(String linkFotoCliente) {
        this.linkFotoCliente = linkFotoCliente;
    }

    public String getUltimaMensagem() {
        return ultimaMensagem;
    }

    public void setUltimaMensagem(String ultimaMensagem) {
        this.ultimaMensagem = ultimaMensagem;
    }

    public int getNovasMensagens() {
        return novasMensagens;
    }

    public void setNovasMensagens(int novasMensagens) {
        this.novasMensagens = novasMensagens;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getDataUltimaMensagem() {
        return dataUltimaMensagem;
    }

    public void setDataUltimaMensagem(String dataUltimaMensagem) {
        this.dataUltimaMensagem = dataUltimaMensagem;
    }

}
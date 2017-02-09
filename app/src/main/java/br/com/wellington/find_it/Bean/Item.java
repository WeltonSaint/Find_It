package br.com.wellington.find_it.Bean;

import android.annotation.SuppressLint;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Classe Item
 *
 * @author Wellington
 * @version 1.0 - 02/01/2017.
 */

public class Item implements Serializable {

    private int codigoItem;
    private String nomeItem;
    private String descricaoItem;
    private double latitudeItem;
    private double longitudeItem;
    private double raioItem;
    private String dataCadastro;
    private String categoriaItem;
    private String statusItem;
    private ArrayList<String> linksFotosItem;
    private int codigoUsuario;

    public void setCodigoItem(int codigoItem) {
        this.codigoItem = codigoItem;
    }

    public String getNomeItem() {
        return nomeItem;
    }

    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public String getDescricaoItem() {
        return descricaoItem;
    }

    public void setDescricaoItem(String descricaoItem) {
        this.descricaoItem = descricaoItem;
    }

    public double getLatitudeItem() {
        return latitudeItem;
    }

    public void setLatitudeItem(double latitudeItem) {
        this.latitudeItem = latitudeItem;
    }

    public double getLongitudeItem() {
        return longitudeItem;
    }

    public void setLongitudeItem(double longitudeItem) {
        this.longitudeItem = longitudeItem;
    }

    public double getRaioItem() {
        return raioItem;
    }

    public void setRaioItem(double raioItem) {
        this.raioItem = raioItem;
    }

    public String getDataCadastro() {
        return dataCadastro;
    }

    @SuppressLint("SimpleDateFormat")
    public void setDataCadastro(String dataCadastro) {
        try {
             Date date = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss").parse(dataCadastro);
            this.dataCadastro = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            this.dataCadastro = dataCadastro;
        }

    }

    public String getCategoriaItem() {
        return categoriaItem;
    }

    public void setCategoriaItem(String categoriaItem) {
        this.categoriaItem = categoriaItem;
    }

    public String getStatusItem() {
        return statusItem;
    }

    public void setStatusItem(String statusItem) {
        this.statusItem = statusItem;
    }

    public int getCodigoUsuario() {
        return codigoUsuario;
    }

    public ArrayList<String> getLinksFotosItem() {
        return linksFotosItem;
    }

    public void setLinksFotosItem(ArrayList<String> linksFotosItem) {
        this.linksFotosItem = linksFotosItem;
    }

    public Item(String nomeItem, String descricaoItem, double latitudeItem, double longitudeItem, double raioItem, String categoriaItem, String statusItem, int codigoUsuario) {
        this.nomeItem = nomeItem;
        this.descricaoItem = descricaoItem;
        this.latitudeItem = latitudeItem;
        this.longitudeItem = longitudeItem;
        this.raioItem = raioItem;
        this.categoriaItem = categoriaItem;
        this.statusItem = statusItem;
        this.codigoUsuario = codigoUsuario;
    }

    public Item() {
    }
}

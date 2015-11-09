package com.tormentaLabs.riobus.favorite.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.tormentaLabs.riobus.core.model.LineModel;

import java.sql.Date;

/**
 * @author limazix
 * @since 3.0
 * Created on 24/10/15
 */
@Table(name = "FAVORITE")
public class FavoriteModel extends Model {

    @Column(name = "CREATED_AT")
    public Date createdAt;

    @Column(name = "LINE")
    public LineModel line;

    public FavoriteModel() {
        super();
    }
}
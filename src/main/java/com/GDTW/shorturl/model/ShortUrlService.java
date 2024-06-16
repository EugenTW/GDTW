package com.GDTW.shorturl.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShortUrlService {

    @Autowired
    private ShortUrlJpa shortUrlJpa;

    // Determine whether the short URL is valid or not
    @Transactional(readOnly = true)
    public boolean isShortUrlIdExist(Integer suId) {
        return shortUrlJpa.existsById(suId);
    }




}

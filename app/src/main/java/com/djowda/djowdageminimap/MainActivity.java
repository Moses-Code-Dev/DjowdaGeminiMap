/*
 * Created by the Djowda Project Team
 * Copyright (c) 2017-2025 Djowda. All rights reserved.
 *
 * This file is part of the Djowda Project.
 *
 * Licensed under the Djowda Non-Commercial, Non-Profit License v1.0
 *
 * Permissions:
 * - You may use, modify, and share this file for non-commercial and non-profit purposes only.
 * - Commercial use of this file, in any form, requires prior written permission
 *   from the Djowda Project maintainers.
 *
 * Notes:
 * - This project is community-driven and continuously evolving.
 * - The Djowda Project reserves the right to relicense future versions.
 *
 * Last Modified: 2025-09-10 19:31
 */

package com.djowda.djowdageminimap;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
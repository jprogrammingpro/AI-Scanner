package com.jprogrammingpro.aiscanner.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jprogrammingpro.aiscanner.R
import com.jprogrammingpro.aiscanner.ui.components.BentoCard
import com.jprogrammingpro.aiscanner.ui.components.ScreenTitle

@Composable
fun InsightsScreen(contentPadding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = contentPadding.calculateBottomPadding() + 24.dp),
    ) {
        ScreenTitle(stringResource(R.string.insights_title), Modifier.padding(top = 12.dp))
        Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BentoCard(
                title = stringResource(R.string.insights_expenses),
                body = stringResource(R.string.insights_expenses_body),
                icon = Icons.Outlined.PieChart,
                highlighted = true,
                minHeight = 190.dp,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BentoCard(
                    title = stringResource(R.string.insights_tables),
                    body = stringResource(R.string.insights_tables_body),
                    icon = Icons.Outlined.TableChart,
                    minHeight = 180.dp,
                    modifier = Modifier.weight(1f),
                )
                BentoCard(
                    title = stringResource(R.string.insights_deadlines),
                    body = stringResource(R.string.insights_deadlines_body),
                    icon = Icons.Outlined.Event,
                    minHeight = 180.dp,
                    modifier = Modifier.weight(1f),
                )
            }
            BentoCard(
                title = stringResource(R.string.insights_contacts),
                body = stringResource(R.string.insights_contacts_body),
                icon = Icons.Outlined.ContactPage,
                minHeight = 120.dp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

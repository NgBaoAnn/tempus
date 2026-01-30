package com.projectapp.tempus.ui.setting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.projectapp.tempus.R
import com.projectapp.tempus.ui.theme.TempusDesignSystem
import com.projectapp.tempus.ui.theme.TempusTheme


class LegalDocumentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DOCUMENT_TYPE = "document_type"
        const val TYPE_PRIVACY_POLICY = "privacy_policy"
        const val TYPE_TERMS_OF_SERVICE = "terms_of_service"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val documentType = intent.getStringExtra(EXTRA_DOCUMENT_TYPE) ?: TYPE_PRIVACY_POLICY
        
        setContent {
            TempusTheme {
                Scaffold { paddingValues ->
                    when (documentType) {
                        TYPE_PRIVACY_POLICY -> PrivacyPolicyScreen(
                            onBackClick = { finish() },
                            modifier = Modifier.padding(paddingValues)
                        )
                        TYPE_TERMS_OF_SERVICE -> TermsOfServiceScreen(
                            onBackClick = { finish() },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        
        LegalHeader(
            title = stringResource(R.string.legal_privacy_policy),
            onBackClick = onBackClick
        )
        
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_intro_title))
                LegalParagraph(stringResource(R.string.privacy_intro_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_info_title))
                LegalSubsection(stringResource(R.string.privacy_info_sub1))
                LegalParagraph(stringResource(R.string.privacy_info_content1))
                
                LegalSubsection(stringResource(R.string.privacy_info_sub2))
                LegalParagraph(stringResource(R.string.privacy_info_content2))
                
                LegalSubsection(stringResource(R.string.privacy_info_sub3))
                LegalParagraph(stringResource(R.string.privacy_info_content3))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_usage_title))
                LegalParagraph(stringResource(R.string.privacy_usage_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_security_title))
                LegalParagraph(stringResource(R.string.privacy_security_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_rights_title))
                LegalParagraph(stringResource(R.string.privacy_rights_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_storage_title))
                LegalParagraph(stringResource(R.string.privacy_storage_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_changes_title))
                LegalParagraph(stringResource(R.string.privacy_changes_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.privacy_contact_title))
                LegalParagraph(stringResource(R.string.privacy_contact_content))
            }
            
            LegalFooter(stringResource(R.string.legal_last_updated, "25/01/2026"))
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun TermsOfServiceScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        
        LegalHeader(
            title = stringResource(R.string.legal_terms_service),
            onBackClick = onBackClick
        )
        
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_accept_title))
                LegalParagraph(stringResource(R.string.terms_accept_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_desc_title))
                LegalParagraph(stringResource(R.string.terms_desc_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_account_title))
                LegalParagraph(stringResource(R.string.terms_account_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_rules_title))
                LegalParagraph(stringResource(R.string.terms_rules_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_ip_title))
                LegalParagraph(stringResource(R.string.terms_ip_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_liability_title))
                LegalParagraph(stringResource(R.string.terms_liability_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_termination_title))
                LegalParagraph(stringResource(R.string.terms_termination_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_changes_title))
                LegalParagraph(stringResource(R.string.terms_changes_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_law_title))
                LegalParagraph(stringResource(R.string.terms_law_content))
            }
            
            LegalCard {
                LegalSectionTitle(stringResource(R.string.terms_contact_title))
                LegalParagraph(stringResource(R.string.terms_contact_content))
            }
            
            LegalFooter(stringResource(R.string.legal_last_updated, "25/01/2026"))
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun LegalHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = onBackClick,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = "← " + stringResource(R.string.back),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 17.sp
            )
        }

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .padding(end = 60.dp)
        )
    }
}

@Composable
private fun LegalCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun LegalSectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
private fun LegalSubsection(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun LegalParagraph(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 22.sp
    )
}

@Composable
private fun LegalFooter(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        )
    }
}

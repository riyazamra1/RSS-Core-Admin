package com.riyaz.rsscoreadmin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.riyaz.rsscoreadmin.data.*
import com.riyaz.rsscoreadmin.ui.navigation.AdminDestination
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun LoginScreen(otp: Boolean, busy: Boolean, error: String?, onLogin: (String,String)->Unit, onVerify: (String)->Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Text("RSS Core Admin", style = MaterialTheme.typography.headlineLarge)
        Text("Secure administrator control center", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        if (!otp) {
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label={Text("Administrator email")}, singleLine=true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), label={Text("Password")}, singleLine=true, visualTransformation=PasswordVisualTransformation())
            Spacer(Modifier.height(16.dp))
            Button(onClick={onLogin(email,password)}, enabled=!busy && email.isNotBlank() && password.isNotBlank(), Modifier.fillMaxWidth()) { Text(if(busy) "Signing in…" else "Sign in") }
        } else {
            Text("Verification code", style=MaterialTheme.typography.titleLarge)
            Text("Enter the six-digit code sent to the administrator email.")
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { code = it.filter(Char::isDigit).take(6) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("OTP") },
                prefix = { Text("RSC-") },
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick={onVerify(code)}, enabled=!busy && code.length==6, Modifier.fillMaxWidth()) { Text(if(busy) "Verifying…" else "Verify and enter") }
        }
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp)); Text(error, color=MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun CommandCenterScreen(health: EndpointHealth?, status: CoreStatusResult?, projects: CoreProjectsResult?, onRefresh: ()->Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
        item { Text("Command Center", style=MaterialTheme.typography.headlineMedium); Text("Central RSS ecosystem administration and live service monitoring.", color=MaterialTheme.colorScheme.onSurfaceVariant) }
        item { ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text("CORE HEALTH", style=MaterialTheme.typography.labelLarge)
            StatusRow("API", health?.let(::stateText) ?: "Checking…")
            StatusRow("HTTP", health?.httpStatus?.toString() ?: "—")
            StatusRow("Latency", health?.responseMs?.let{"$it ms"} ?: "—")
            StatusRow("Version", (status as? CoreStatusResult.Available)?.version ?: "—")
            Button(onClick=onRefresh) { Text("Refresh live status") }
        } } }
        item { ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Text("PROJECT REGISTRY", style=MaterialTheme.typography.titleMedium)
            when(projects) {
                is CoreProjectsResult.Available -> projects.projects.forEach { Text("• $it") }
                is CoreProjectsResult.Unavailable -> Text(projects.reason, color=MaterialTheme.colorScheme.error)
                null -> Text("Not checked")
            }
        } } }
        item { ElevatedCard { Column(Modifier.padding(18.dp), verticalArrangement=Arrangement.spacedBy(7.dp)) {
            Text("ADMIN CONTROL SURFACE", style=MaterialTheme.typography.titleMedium)
            Text("Accounts • Projects • Pricing • Entitlements • Features • Remote Config • Limits • Maintenance • Ads • Audit • System • Security • Settings")
            Text("All privileged operations use the authenticated RSS Core admin API.", color=MaterialTheme.colorScheme.onSurfaceVariant)
        } } }
    }
}

@Composable
fun AdminModuleScreen(destination: AdminDestination, data: JSONObject?, error: String?, busy: Boolean,
    mutate: (AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload:(AdminDestination)->Unit) {
    var query by remember(destination) { mutableStateOf("") }
    var dialog by remember(destination) { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text(destination.title, style=MaterialTheme.typography.headlineMedium)
            Text(description(destination), color=MaterialTheme.colorScheme.onSurfaceVariant)
            if (!error.isNullOrBlank()) Text(error, color=MaterialTheme.colorScheme.error)
        }
        if (destination in listOf(AdminDestination.ACCOUNTS,AdminDestination.PROJECTS,AdminDestination.PRICING,AdminDestination.ENTITLEMENTS,AdminDestination.FEATURES,AdminDestination.CONFIG,AdminDestination.LIMITS,AdminDestination.MAINTENANCE,AdminDestination.ADS,AdminDestination.AUDIT)) {
            item { OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text("Search / filter")},singleLine=true, trailingIcon={Icon(Icons.Default.Search,null)}) }
        }
        when(destination) {
            AdminDestination.ACCOUNTS -> AccountsContent(data,query,mutate,reload)
            AdminDestination.PROJECTS -> ProjectsContent(data,query,mutate,reload)
            AdminDestination.PRICING -> PricingContent(data,query,mutate,reload)
            AdminDestination.ENTITLEMENTS -> EntitlementsContent(data,query,mutate,reload)
            AdminDestination.FEATURES -> FeatureContent(data,query,mutate,reload)
            AdminDestination.CONFIG -> ConfigContent(data,query,mutate,reload)
            AdminDestination.LIMITS -> LimitsContent(data,query,mutate,reload)
            AdminDestination.MAINTENANCE -> MaintenanceContent(data,query,mutate,reload)
            AdminDestination.ADS -> AdsContent(data,query,mutate,reload)
            AdminDestination.AUDIT -> AuditContent(data,query)
            AdminDestination.SYSTEM -> SystemContent(data)
            else -> Unit
        }
        if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
    }
    if(dialog!=null) AlertDialog(onDismissRequest={dialog=null}, confirmButton={TextButton(onClick={dialog=null}){Text("Close")}}, title={Text(dialog ?: "")}, text={Text("Use the controls in this module to perform the authenticated server operation.")})
}

@Composable private fun AccountsContent(d:JSONObject?, q:String, mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload:(AdminDestination)->Unit) {
    val rows=d?.optJSONArray("requests") ?: JSONArray()
    Section("Pending RSS Core accounts") {
        if(rows.length()==0) Text("No pending RSS Core registration requests.")
        for(i in 0 until rows.length()) {
            val x=rows.optJSONObject(i); if(x!=null && matches(x,q)) ActionRow(x.optString("name","Unnamed"),x.optString("email"),"Approve","Reject",
                {mutate(AdminDestination.ACCOUNTS,"POST","/api/v1/admin/registration-requests/"+x.optString("id")+"/approve",JSONObject(),{reload(AdminDestination.ACCOUNTS)})},
                {mutate(AdminDestination.ACCOUNTS,"POST","/api/v1/admin/registration-requests/"+x.optString("id")+"/reject",JSONObject(),{reload(AdminDestination.ACCOUNTS)})})
        }
    }
    Section("RSS AI Project Manager approvals") {
        val approvals = d?.optJSONArray("approval_requests") ?: JSONArray()
        if (approvals.length() == 0) {
            Text("No pending RSS AI Project Manager approval requests.")
        }
        for (i in 0 until approvals.length()) {
            val x = approvals.optJSONObject(i) ?: continue
            if (matches(x, q)) {
                ActionRow(
                    x.optString("name", x.optString("email", "Unnamed")),
                    x.optString("email") + " • " + x.optString("status", "pending"),
                    "Approve",
                    "Reject",
                    {
                        mutate(
                            AdminDestination.ACCOUNTS,
                            "POST",
                            "/api/v1/admin/project-manager/approval-requests/" + x.optString("id") + "/approve",
                            JSONObject(),
                            { reload(AdminDestination.ACCOUNTS) }
                        )
                    },
                    {
                        mutate(
                            AdminDestination.ACCOUNTS,
                            "POST",
                            "/api/v1/admin/project-manager/approval-requests/" + x.optString("id") + "/reject",
                            JSONObject(),
                            { reload(AdminDestination.ACCOUNTS) }
                        )
                    }
                )
            }
        }
    }
}

@Composable private fun ProjectsContent(d:JSONObject?, q:String, mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload:(AdminDestination)->Unit) {
    val rows=d?.optJSONArray("projects") ?: JSONArray()
    Button(onClick={ mutate(AdminDestination.PROJECTS,"POST","/api/v1/admin/projects",JSONObject().put("key","new-project").put("name","New Project").put("active",true),{reload(AdminDestination.PROJECTS)}) }) { Text("Add project") }
    for(i in 0 until rows.length()) {
        val x=rows.optJSONObject(i) ?: continue
        if(matches(x,q)) ActionRow(x.optString("name"),x.optString("key")+" • "+if(x.optBoolean("active"))"ACTIVE" else "INACTIVE","Deactivate","",
            {mutate(AdminDestination.PROJECTS,"DELETE","/api/v1/admin/projects/"+x.optString("key"),JSONObject(),{reload(AdminDestination.PROJECTS)})},{})
    }
}

@Composable private fun PricingContent(d:JSONObject?, q:String, mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload:(AdminDestination)->Unit) {
    Text("Central pricing • LKR • server-confirmed changes",style=MaterialTheme.typography.titleMedium)
    Text("Project · Duration · Days · List price · Discount · Discount value · Sale start · Sale end · Active · Edit",color=MaterialTheme.colorScheme.onSurfaceVariant)
    val rows=d?.optJSONArray("plans") ?: JSONArray()
    for(i in 0 until rows.length()) {
        val x=rows.optJSONObject(i) ?: continue
        if(!matches(x,q)) continue
        ElevatedCard { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
            Text(x.optString("project_name"),style=MaterialTheme.typography.titleMedium)
            Text(x.optString("name")+" • "+x.optString("plan_key"))
            StatusRow("Duration",x.optString("billing_period")+" / "+x.optInt("duration_days")+" days")
            StatusRow("List price","LKR "+x.optInt("list_price_lkr"))
            StatusRow("Discount",x.optString("discount_type")+" / "+x.optInt("discount_value"))
            StatusRow("Sale",x.optString("sale_start_at","—")+" → "+x.optString("sale_end_at","—"))
            StatusRow("Status",if(x.optBoolean("active"))"ACTIVE" else "INACTIVE")
            Button(onClick={ mutate(AdminDestination.PRICING,"PUT","/api/v1/admin/pricing",JSONObject()
                .put("project_key",x.optString("project_key")).put("plan_key",x.optString("plan_key")).put("name",x.optString("name"))
                .put("billing_period",x.optString("billing_period")).put("duration_days",x.optInt("duration_days"))
                .put("currency",x.optString("currency","LKR")).put("list_price_lkr",x.optInt("list_price_lkr"))
                .put("discount_type",x.optString("discount_type","none")).put("discount_value",x.optInt("discount_value"))
                .put("sale_start_at",x.optString("sale_start_at").ifBlank{JSONObject.NULL.toString()}).put("sale_end_at",x.optString("sale_end_at").ifBlank{JSONObject.NULL.toString()})
                .put("active",!x.optBoolean("active")),{reload(AdminDestination.PRICING)})}) { Text(if(x.optBoolean("active"))"Deactivate" else "Activate") }
        } }
    }
}

@Composable private fun EntitlementsContent(d:JSONObject?, q:String, mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload:(AdminDestination)->Unit) {
    Text("Grant premium, family, staff, promotional, beta or complimentary access.",color=MaterialTheme.colorScheme.onSurfaceVariant)
    val rows=d?.optJSONArray("entitlements") ?: JSONArray()
    for(i in 0 until rows.length()) {
        val x=rows.optJSONObject(i) ?: continue
        if(matches(x,q)) ActionRow(x.optString("customer_email"),x.optString("project_key")+" • "+x.optString("source"),"Revoke","",
            {mutate(AdminDestination.ENTITLEMENTS,"DELETE","/api/v1/admin/entitlements",JSONObject().put("customer_email",x.optString("customer_email")).put("project_key",x.optString("project_key")),{reload(AdminDestination.ENTITLEMENTS)})},{})
    }
    var email by remember { mutableStateOf("") }
    var project by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("family") }
    var plan by remember { mutableStateOf("family") }
    OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Customer email")})
    OutlinedTextField(project,{project=it},Modifier.fillMaxWidth(),label={Text("Project key (blank = all)")})
    OutlinedTextField(source,{source=it},Modifier.fillMaxWidth(),label={Text("Source")})
    OutlinedTextField(plan,{plan=it},Modifier.fillMaxWidth(),label={Text("Plan key")})
    Button(onClick={mutate(AdminDestination.ENTITLEMENTS,"PUT","/api/v1/admin/entitlements",JSONObject().put("customer_email",email).put("project_key",project).put("all_projects",project.isBlank()).put("source",source).put("plan_key",plan),{reload(AdminDestination.ENTITLEMENTS)})},enabled=email.isNotBlank()) { Text("Grant access") }
}

@Composable private fun FeatureContent(d:JSONObject?,q:String,mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit,reload:(AdminDestination)->Unit) {
    val rows=d?.optJSONArray("features") ?: JSONArray()
    for(i in 0 until rows.length()){val x=rows.optJSONObject(i)?:continue;if(matches(x,q)) ElevatedCard{Column(Modifier.padding(14.dp)){Text(x.optString("project_name")+" • "+x.optString("feature_key"),style=MaterialTheme.typography.titleMedium);Text((if(x.optBoolean("enabled"))"Enabled" else "Disabled")+" • rollout "+x.optInt("rollout_percent")+"%");Button(onClick={mutate(AdminDestination.FEATURES,"PUT","/api/v1/admin/features",JSONObject().put("project_key",x.optString("project_key")).put("feature_key",x.optString("feature_key")).put("name",x.optString("name")).put("description",x.optString("description")).put("enabled",!x.optBoolean("enabled")).put("rollout_percent",x.optInt("rollout_percent")),{reload(AdminDestination.FEATURES)})}){Text(if(x.optBoolean("enabled"))"Disable" else "Enable")}}}}
    var project by remember{mutableStateOf("")};var key by remember{mutableStateOf("")};var name by remember{mutableStateOf("")}
    OutlinedTextField(project,{project=it},Modifier.fillMaxWidth(),label={Text("Project key")});OutlinedTextField(key,{key=it},Modifier.fillMaxWidth(),label={Text("Feature key")});OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Feature name")})
    Button(onClick={mutate(AdminDestination.FEATURES,"PUT","/api/v1/admin/features",JSONObject().put("project_key",project).put("feature_key",key).put("name",name).put("enabled",true).put("rollout_percent",100),{reload(AdminDestination.FEATURES)})}){Text("Create / update feature")}
}

@Composable private fun ConfigContent(d:JSONObject?,q:String,mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit,reload:(AdminDestination)->Unit){
    val rows=d?.optJSONArray("config")?:JSONArray();for(i in 0 until rows.length()){val x=rows.optJSONObject(i)?:continue;if(matches(x,q)) ElevatedCard{Column(Modifier.padding(14.dp)){Text(x.optString("project_name")+" • "+x.optString("config_key"),style=MaterialTheme.typography.titleMedium);Text(x.optString("value_json"));}}}
    var project by remember{mutableStateOf("")};var key by remember{mutableStateOf("")};var value by remember{mutableStateOf("")};var type by remember{mutableStateOf("string")}
    OutlinedTextField(project,{project=it},Modifier.fillMaxWidth(),label={Text("Project key")});OutlinedTextField(key,{key=it},Modifier.fillMaxWidth(),label={Text("Config key")});OutlinedTextField(value,{value=it},Modifier.fillMaxWidth(),label={Text("Value")});OutlinedTextField(type,{type=it},Modifier.fillMaxWidth(),label={Text("Type: string/number/boolean/json")})
    Button(onClick={mutate(AdminDestination.CONFIG,"PUT","/api/v1/admin/config",JSONObject().put("project_key",project).put("config_key",key).put("value_json",value).put("value_type",type),{reload(AdminDestination.CONFIG)})}){Text("Save remote config")}
}

@Composable private fun LimitsContent(d:JSONObject?,q:String,mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit,reload:(AdminDestination)->Unit){
    val rows=d?.optJSONArray("limits")?:JSONArray();for(i in 0 until rows.length()){val x=rows.optJSONObject(i)?:continue;if(matches(x,q)) ElevatedCard{Column(Modifier.padding(14.dp)){Text(x.optString("project_name")+" • "+x.optString("plan_key"),style=MaterialTheme.typography.titleMedium);Text(x.optString("limit_key")+" = "+x.optInt("limit_value")+" "+x.optString("unit"))}}}
    var project by remember{mutableStateOf("")};var plan by remember{mutableStateOf("free")};var key by remember{mutableStateOf("")};var value by remember{mutableStateOf("0")};var unit by remember{mutableStateOf("count")}
    OutlinedTextField(project,{project=it},Modifier.fillMaxWidth(),label={Text("Project key")});OutlinedTextField(plan,{plan=it},Modifier.fillMaxWidth(),label={Text("Plan key")});OutlinedTextField(key,{key=it},Modifier.fillMaxWidth(),label={Text("Limit key")});OutlinedTextField(value,{value=it},Modifier.fillMaxWidth(),label={Text("Limit value")});OutlinedTextField(unit,{unit=it},Modifier.fillMaxWidth(),label={Text("Unit")})
    Button(onClick={mutate(AdminDestination.LIMITS,"PUT","/api/v1/admin/limits",JSONObject().put("project_key",project).put("plan_key",plan).put("limit_key",key).put("limit_value",value.toIntOrNull()?:0).put("unit",unit),{reload(AdminDestination.LIMITS)})}){Text("Save limit")}
}

@Composable private fun MaintenanceContent(d:JSONObject?,q:String,mutate:(AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit,reload:(AdminDestination)->Unit){
    val rows=d?.optJSONArray("maintenance")?:JSONArray();for(i in 0 until rows.length()){val x=rows.optJSONObject(i)?:continue;if(matches(x,q)) ElevatedCard{Column(Modifier.padding(14.dp)){Text(x.optString("project_name"),style=MaterialTheme.typography.titleMedium);Text((if(x.optBoolean("enabled"))"MAINTENANCE ON" else "Normal")+" • min "+x.optString("min_supported_version","—"));Text(x.optString("message",""));}}}
    var project by remember{mutableStateOf("")};var enabled by remember{mutableStateOf(false)};var version by remember{mutableStateOf("")};var message by remember{mutableStateOf("")}
    OutlinedTextField(project,{project=it},Modifier.fillMaxWidth(),label={Text("Project key")});Row(verticalAlignment=Alignment.CenterVertically){Checkbox(enabled,{enabled=it});Text("Maintenance enabled")};OutlinedTextField(version,{version=it},Modifier.fillMaxWidth(),label={Text("Minimum supported version")});OutlinedTextField(message,{message=it},Modifier.fillMaxWidth(),label={Text("Maintenance message")})
    Button(onClick={mutate(AdminDestination.MAINTENANCE,"PUT","/api/v1/admin/maintenance",JSONObject().put("project_key",project).put("enabled",enabled).put("min_supported_version",version).put("message",message),{reload(AdminDestination.MAINTENANCE)})}){Text("Save maintenance")}
}

@Composable
private fun AdsContent(d: JSONObject?, q: String, mutate: (AdminDestination,String,String,JSONObject,(() -> Unit)?)->Unit, reload: (AdminDestination)->Unit) {
    Section("Advertising") {
        Text("Providers, placements, formats, test mode, frequency and session limits.")
        val rows = d?.optJSONArray("configs") ?: JSONArray()
        for (i in 0 until rows.length()) {
            val x = rows.optJSONObject(i) ?: continue
            if (matches(x, q)) {
                Text(x.optString("project_name") + " • " + x.optString("provider_key") + " • " + x.optString("placement_key"))
                val enabledText = if (x.optBoolean("enabled")) "Enabled" else "Disabled"
                val modeText = if (x.optBoolean("test_mode")) "Test" else "Production"
                Text(x.optString("ad_format") + " • " + enabledText + " • " + modeText)
                Text("Frequency " + x.optInt("frequency_seconds") + "s • max " + x.optInt("max_ads_per_session"))
                Button(onClick = {
                    val body = JSONObject()
                        .put("project_key", x.optString("project_key"))
                        .put("provider_key", x.optString("provider_key"))
                        .put("placement_key", x.optString("placement_key"))
                        .put("ad_format", x.optString("ad_format"))
                        .put("test_mode", x.optBoolean("test_mode"))
                        .put("frequency_seconds", x.optInt("frequency_seconds"))
                        .put("max_ads_per_session", x.optInt("max_ads_per_session"))
                        .put("placement_enabled", !x.optBoolean("enabled"))
                    mutate(AdminDestination.ADS, "PUT", "/api/v1/admin/ads", body, { reload(AdminDestination.ADS) })
                }) { Text(if (x.optBoolean("enabled")) "Disable" else "Enable") }
            }
        }
    }
}

@Composable private fun AuditContent(d:JSONObject?,q:String){
    val rows=d?.optJSONArray("audit")?:JSONArray();for(i in 0 until rows.length()){val x=rows.optJSONObject(i)?:continue;if(matches(x,q))ElevatedCard{Column(Modifier.padding(14.dp)){Text(x.optString("action")+" • "+x.optString("target_type"),style=MaterialTheme.typography.titleMedium);Text(x.optString("target_key"));Text(x.optString("admin_email")+" • "+x.optString("created_at"),color=MaterialTheme.colorScheme.onSurfaceVariant)}}}
}

@Composable private fun SystemContent(d:JSONObject?){ElevatedCard{Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){Text("RSS Core System",style=MaterialTheme.typography.titleMedium);d?.keys()?.forEach{key->Text(key+": "+d.optString(key))}}}}

@Composable fun SecurityScreen(onLogout:()->Unit){LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Security",style=MaterialTheme.typography.headlineMedium)};item{Section("Administrator authentication"){Text("Password + email OTP");Text("Server-side session with expiry");Text("Encrypted Android session storage");Text("HTTPS-only transport")}};item{Button(onClick=onLogout){Text("Sign out")}}}}
@Composable fun SettingsScreen(){var compact by remember{mutableStateOf(false)};var refresh by remember{mutableStateOf(true)};LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Settings",style=MaterialTheme.typography.headlineMedium)};item{Section("Mobile operator preferences"){Row(verticalAlignment=Alignment.CenterVertically){Switch(compact,{compact=it});Text("Compact lists")};Row(verticalAlignment=Alignment.CenterVertically){Switch(refresh,{refresh=it});Text("Live refresh on command center")};Text("Session token is stored using Android encrypted preferences.")}}}}

@Composable private fun Section(title:String,content:@Composable ColumnScope.()->Unit){ElevatedCard{Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,style=MaterialTheme.typography.titleMedium);content()}}}
@Composable private fun StatusRow(label:String,value:String){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Text(value,color=MaterialTheme.colorScheme.onSurfaceVariant)}}
@Composable private fun ActionRow(title:String,subtitle:String,primary:String,secondary:String,onPrimary:()->Unit,onSecondary:()->Unit){ElevatedCard{Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(subtitle,color=MaterialTheme.colorScheme.onSurfaceVariant)};if(primary.isNotBlank())Button(onClick=onPrimary){Text(primary)};if(secondary.isNotBlank())OutlinedButton(onClick=onSecondary){Text(secondary)}}}}
private fun matches(x:JSONObject,q:String):Boolean=if(q.isBlank())true else x.toString().contains(q,true)
private fun description(d:AdminDestination)=when(d){AdminDestination.ACCOUNTS->"Approve or reject pending account workflows.";AdminDestination.PROJECTS->"Central RSS project registry.";AdminDestination.PRICING->"Centralized price, discount and sale-window control.";AdminDestination.ENTITLEMENTS->"Grant or revoke premium and family access without purchase.";AdminDestination.FEATURES->"Feature flags and staged rollouts.";AdminDestination.CONFIG->"Non-secret runtime configuration.";AdminDestination.LIMITS->"Plan quotas and usage limits.";AdminDestination.MAINTENANCE->"Maintenance mode and minimum supported versions.";AdminDestination.ADS->"Central advertising providers, placements and policies.";AdminDestination.AUDIT->"Authenticated administrator change history.";AdminDestination.SYSTEM->"Live RSS Core status.";else->"RSS Core administration."}
private fun stateText(h:EndpointHealth):String=when(h.state){HealthState.HEALTHY->"Healthy";HealthState.DEGRADED->"Warning";HealthState.OFFLINE->"Critical";else->"Unknown"}

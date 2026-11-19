# kotlinx.serialization DTOs are looked up by generated serializers, not reflection;
# keep the generated companions so R8 full mode doesn't strip them.
-keepclassmembers @kotlinx.serialization.Serializable class com.harshvardhan.cartwise.data.network.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

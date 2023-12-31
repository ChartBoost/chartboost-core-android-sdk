Android Change Log
==================

### Version 0.4.0 *(2023-12-7)*
Improvements
- Added `partnerConsentStatus: Map<String, ConsentStatus>` to `ConsentManagementPlatform. This is to facilitate per-partner consent for Mediation.
- Added `fun onPartnerConsentStatusChange(partnerId: String, status: ConsentStatus)` to `ConsentObserver`.
- Added ability for Core to automatically subscribe `PublisherMetadataObserver` modules.

### Version 0.3.0 *(2023-10-12)*
Improvements:
- Added `PublisherMetadataObserver`s to receive notifications that publisher metadata has changed.

Bug Fixes:
- Made several environment getters async to ensure reliability.

### Version 0.2.0 *(2023-8-29)*
Improvements:
- Split out Usercentrics adapter.
- Implemented most of the minor features.

### Version 0.1.0 *(2023-8-23)*
Improvements:
- Initial version with basic functionality.

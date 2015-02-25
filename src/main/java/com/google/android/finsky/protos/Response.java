package com.google.android.finsky.protos;

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.google.protobuf.nano.WireFormatNano;

import java.io.IOException;

public abstract interface Response {
    public static final class Payload extends MessageNano
    {
//        public Tos.AcceptTosResponse acceptTosResponse;
//        public AckNotification.AckNotificationResponse ackNotificationResponse;
//        public Restore.GetBackupDeviceChoicesResponse backupDeviceChoicesResponse;
//        public Restore.GetBackupDocumentChoicesResponse backupDocumentChoicesResponse;
//        public BuyInstruments.BillingProfileResponse billingProfileResponse;
//        public Browse.BrowseResponse browseResponse;
        public Details.BulkDetailsResponse bulkDetailsResponse;
//        public Buy.BuyResponse buyResponse;
//        public ChallengeAction.ChallengeResponse challengeResponse;
//        public BuyInstruments.CheckIabPromoResponse checkIabPromoResponse;
//        public BuyInstruments.CheckInstrumentResponse checkInstrumentResponse;
//        public CheckPromoOffer.CheckPromoOfferResponse checkPromoOfferResponse;
//        public Purchase.CommitPurchaseResponse commitPurchaseResponse;
//        public ConsumePurchaseResponse consumePurchaseResponse;
//        public BuyInstruments.CreateInstrumentResponse createInstrumentResponse;
//        public DebugSettings.DebugSettingsResponse debugSettingsResponse;
//        public Delivery.DeliveryResponse deliveryResponse;
        public Details.DetailsResponse detailsResponse;
//        public EarlyUpdate.EarlyUpdateResponse earlyUpdateResponse;
//        public ContentFlagging.FlagContentResponse flagContentResponse;
//        public BuyInstruments.GetInitialInstrumentFlowStateResponse getInitialInstrumentFlowStateResponse;
//        public CarrierBilling.InitiateAssociationResponse initiateAssociationResponse;
//        public BuyInstruments.InstrumentSetupInfoResponse instrumentSetupInfoResponse;
//        public LibraryReplication.LibraryReplicationResponse libraryReplicationResponse;
        public DocList.ListResponse listResponse;
//        public Log.LogResponse logResponse;
//        public ModifyLibrary.ModifyLibraryResponse modifyLibraryResponse;
//        public PlusOne.PlusOneResponse plusOneResponse;
//        public PlusProfile.PlusProfileResponse plusProfileResponse;
//        public Preloads.PreloadsResponse preloadsResponse;
//        public Purchase.PreparePurchaseResponse preparePurchaseResponse;
//        public Buy.PurchaseStatusResponse purchaseStatusResponse;
//        public RateSuggestedContentResponse rateSuggestedContentResponse;
//        public UserActivity.RecordUserActivityResponse recordUserActivityResponse;
//        public PromoCode.RedeemCodeResponse redeemCodeResponse;
//        public BuyInstruments.RedeemGiftCardResponse redeemGiftCardResponse;
        public ResolveLink.ResolvedLink resolveLinkResponse;
//        public Rev.ReviewResponse reviewResponse;
//        public RevokeResponse revokeResponse;
        public Search.SearchResponse searchResponse;
//        public SearchSuggest.SearchSuggestResponse searchSuggestResponse;
//        public SelfUpdate.SelfUpdateResponse selfUpdateResponse;
//        public Toc.TocResponse tocResponse;
//        public BuyInstruments.UpdateInstrumentResponse updateInstrumentResponse;
//        public UploadDeviceConfig.UploadDeviceConfigResponse uploadDeviceConfigResponse;
//        public UserActivity.UserActivitySettingsResponse userActivitySettingsResponse;
//        public CarrierBilling.VerifyAssociationResponse verifyAssociationResponse;

        public Payload() {
            super();
            this.clear();
        }

        public Payload clear() {
            this.listResponse = null;
            this.detailsResponse = null;
//            this.reviewResponse = null;
//            this.buyResponse = null;
            this.searchResponse = null;
//            this.tocResponse = null;
//            this.browseResponse = null;
//            this.purchaseStatusResponse = null;
//            this.updateInstrumentResponse = null;
//            this.logResponse = null;
//            this.checkInstrumentResponse = null;
//            this.plusOneResponse = null;
//            this.flagContentResponse = null;
//            this.ackNotificationResponse = null;
//            this.initiateAssociationResponse = null;
//            this.verifyAssociationResponse = null;
//            this.libraryReplicationResponse = null;
//            this.revokeResponse = null;
            this.bulkDetailsResponse = null;
            this.resolveLinkResponse = null;
//            this.deliveryResponse = null;
//            this.acceptTosResponse = null;
//            this.rateSuggestedContentResponse = null;
//            this.checkPromoOfferResponse = null;
//            this.instrumentSetupInfoResponse = null;
//            this.redeemGiftCardResponse = null;
//            this.modifyLibraryResponse = null;
//            this.uploadDeviceConfigResponse = null;
//            this.plusProfileResponse = null;
//            this.consumePurchaseResponse = null;
//            this.billingProfileResponse = null;
//            this.preparePurchaseResponse = null;
//            this.commitPurchaseResponse = null;
//            this.debugSettingsResponse = null;
//            this.checkIabPromoResponse = null;
//            this.userActivitySettingsResponse = null;
//            this.recordUserActivityResponse = null;
//            this.redeemCodeResponse = null;
//            this.selfUpdateResponse = null;
//            this.searchSuggestResponse = null;
//            this.getInitialInstrumentFlowStateResponse = null;
//            this.createInstrumentResponse = null;
//            this.challengeResponse = null;
//            this.backupDeviceChoicesResponse = null;
//            this.backupDocumentChoicesResponse = null;
//            this.earlyUpdateResponse = null;
//            this.preloadsResponse = null;
            this.cachedSize = -1;
            return this;
        }

        @Override
        protected int computeSerializedSize() {
            int computeSerializedSize;
            computeSerializedSize = super.computeSerializedSize();
            if (this.listResponse != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(1, this.listResponse);
            }
            if (this.detailsResponse != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(2, this.detailsResponse);
            }
//            if (this.reviewResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(3, this.reviewResponse);
//            }
//            if (this.buyResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(4, this.buyResponse);
//            }
            if (this.searchResponse != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(5, this.searchResponse);
            }
//            if (this.tocResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(6, this.tocResponse);
//            }
//            if (this.browseResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(7, this.browseResponse);
//            }
//            if (this.purchaseStatusResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(8, this.purchaseStatusResponse);
//            }
//            if (this.updateInstrumentResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(9, this.updateInstrumentResponse);
//            }
//            if (this.logResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(10, this.logResponse);
//            }
//            if (this.checkInstrumentResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(11, this.checkInstrumentResponse);
//            }
//            if (this.plusOneResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(12, this.plusOneResponse);
//            }
//            if (this.flagContentResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(13, this.flagContentResponse);
//            }
//            if (this.ackNotificationResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(14, this.ackNotificationResponse);
//            }
//            if (this.initiateAssociationResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(15, this.initiateAssociationResponse);
//            }
//            if (this.verifyAssociationResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(16, this.verifyAssociationResponse);
//            }
//            if (this.libraryReplicationResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(17, this.libraryReplicationResponse);
//            }
//            if (this.revokeResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(18, this.revokeResponse);
//            }
            if (this.bulkDetailsResponse != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(19, this.bulkDetailsResponse);
            }
            if (this.resolveLinkResponse != null) {
                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(20, this.resolveLinkResponse);
            }
//            if (this.deliveryResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(21, this.deliveryResponse);
//            }
//            if (this.acceptTosResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(22, this.acceptTosResponse);
//            }
//            if (this.rateSuggestedContentResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(23, this.rateSuggestedContentResponse);
//            }
//            if (this.checkPromoOfferResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(24, this.checkPromoOfferResponse);
//            }
//            if (this.instrumentSetupInfoResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(25, this.instrumentSetupInfoResponse);
//            }
//            if (this.redeemGiftCardResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(26, this.redeemGiftCardResponse);
//            }
//            if (this.modifyLibraryResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(27, this.modifyLibraryResponse);
//            }
//            if (this.uploadDeviceConfigResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(28, this.uploadDeviceConfigResponse);
//            }
//            if (this.plusProfileResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(29, this.plusProfileResponse);
//            }
//            if (this.consumePurchaseResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(30, this.consumePurchaseResponse);
//            }
//            if (this.billingProfileResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(31, this.billingProfileResponse);
//            }
//            if (this.preparePurchaseResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(32, this.preparePurchaseResponse);
//            }
//            if (this.commitPurchaseResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(33, this.commitPurchaseResponse);
//            }
//            if (this.debugSettingsResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(34, this.debugSettingsResponse);
//            }
//            if (this.checkIabPromoResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(35, this.checkIabPromoResponse);
//            }
//            if (this.userActivitySettingsResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(36, this.userActivitySettingsResponse);
//            }
//            if (this.recordUserActivityResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(37, this.recordUserActivityResponse);
//            }
//            if (this.redeemCodeResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(38, this.redeemCodeResponse);
//            }
//            if (this.selfUpdateResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(39, this.selfUpdateResponse);
//            }
//            if (this.searchSuggestResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(40, this.searchSuggestResponse);
//            }
//            if (this.getInitialInstrumentFlowStateResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(41, this.getInitialInstrumentFlowStateResponse);
//            }
//            if (this.createInstrumentResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(42, this.createInstrumentResponse);
//            }
//            if (this.challengeResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(43, this.challengeResponse);
//            }
//            if (this.backupDeviceChoicesResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(44, this.backupDeviceChoicesResponse);
//            }
//            if (this.backupDocumentChoicesResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(45, this.backupDocumentChoicesResponse);
//            }
//            if (this.earlyUpdateResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(46, this.earlyUpdateResponse);
//            }
//            if (this.preloadsResponse != null) {
//                computeSerializedSize += CodedOutputByteBufferNano.computeMessageSize(47, this.preloadsResponse);
//            }
            return computeSerializedSize;
        }

        @Override
        public Payload mergeFrom(CodedInputByteBufferNano input) throws IOException {
            int tag;
            while (true) {
                tag = input.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(input, tag)) {
                            return this;
                        }
                        break;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        if (this.listResponse == null) {
                            this.listResponse = new DocList.ListResponse();
                        }
                        input.readMessage(this.listResponse);
                        continue;
                    }
                    case 18: {
                        if (this.detailsResponse == null) {
                            this.detailsResponse = new Details.DetailsResponse();
                        }
                        input.readMessage(this.detailsResponse);
                        continue;
                    }
                    case 26: {
//                        if (this.reviewResponse == null) {
//                            this.reviewResponse = new Rev.ReviewResponse();
//                        }
//                        input.readMessage(this.reviewResponse);
                        continue;
                    }
                    case 34: {
//                        if (this.buyResponse == null) {
//                            this.buyResponse = new Buy.BuyResponse();
//                        }
//                        input.readMessage(this.buyResponse);
                        continue;
                    }
                    case 42: {
                        if (this.searchResponse == null) {
                            this.searchResponse = new Search.SearchResponse();
                        }
                        input.readMessage(this.searchResponse);
                        continue;
                    }
                    case 50: {
//                        if (this.tocResponse == null) {
//                            this.tocResponse = new Toc.TocResponse();
//                        }
//                        input.readMessage(this.tocResponse);
                        continue;
                    }
                    case 58: {
//                        if (this.browseResponse == null) {
//                            this.browseResponse = new Browse.BrowseResponse();
//                        }
//                        input.readMessage(this.browseResponse);
                        continue;
                    }
                    case 66: {
//                        if (this.purchaseStatusResponse == null) {
//                            this.purchaseStatusResponse = new Buy.PurchaseStatusResponse();
//                        }
//                        input.readMessage(this.purchaseStatusResponse);
                        continue;
                    }
                    case 74: {
//                        if (this.updateInstrumentResponse == null) {
//                            this.updateInstrumentResponse = new BuyInstruments.UpdateInstrumentResponse();
//                        }
//                        input.readMessage(this.updateInstrumentResponse);
                        continue;
                    }
                    case 82: {
//                        if (this.logResponse == null) {
//                            this.logResponse = new Log.LogResponse();
//                        }
//                        input.readMessage(this.logResponse);
                        continue;
                    }
                    case 90: {
//                        if (this.checkInstrumentResponse == null) {
//                            this.checkInstrumentResponse = new BuyInstruments.CheckInstrumentResponse();
//                        }
//                        input.readMessage(this.checkInstrumentResponse);
                        continue;
                    }
                    case 98: {
//                        if (this.plusOneResponse == null) {
//                            this.plusOneResponse = new PlusOne.PlusOneResponse();
//                        }
//                        input.readMessage(this.plusOneResponse);
                        continue;
                    }
                    case 106: {
//                        if (this.flagContentResponse == null) {
//                            this.flagContentResponse = new ContentFlagging.FlagContentResponse();
//                        }
//                        input.readMessage(this.flagContentResponse);
                        continue;
                    }
                    case 114: {
//                        if (this.ackNotificationResponse == null) {
//                            this.ackNotificationResponse = new AckNotification.AckNotificationResponse();
//                        }
//                        input.readMessage(this.ackNotificationResponse);
                        continue;
                    }
                    case 122: {
//                        if (this.initiateAssociationResponse == null) {
//                            this.initiateAssociationResponse = new CarrierBilling.InitiateAssociationResponse();
//                        }
//                        input.readMessage(this.initiateAssociationResponse);
                        continue;
                    }
                    case 130: {
//                        if (this.verifyAssociationResponse == null) {
//                            this.verifyAssociationResponse = new CarrierBilling.VerifyAssociationResponse();
//                        }
//                        input.readMessage(this.verifyAssociationResponse);
                        continue;
                    }
                    case 138: {
//                        if (this.libraryReplicationResponse == null) {
//                            this.libraryReplicationResponse = new LibraryReplication.LibraryReplicationResponse();
//                        }
//                        input.readMessage(this.libraryReplicationResponse);
                        continue;
                    }
                    case 146: {
//                        if (this.revokeResponse == null) {
//                            this.revokeResponse = new RevokeResponse();
//                        }
//                        input.readMessage(this.revokeResponse);
                        continue;
                    }
                    case 154: {
                        if (this.bulkDetailsResponse == null) {
                            this.bulkDetailsResponse = new Details.BulkDetailsResponse();
                        }
                        input.readMessage(this.bulkDetailsResponse);
                        continue;
                    }
                    case 162: {
                        if (this.resolveLinkResponse == null) {
                            this.resolveLinkResponse = new ResolveLink.ResolvedLink();
                        }
                        input.readMessage(this.resolveLinkResponse);
                        continue;
                    }
                    case 170: {
//                        if (this.deliveryResponse == null) {
//                            this.deliveryResponse = new Delivery.DeliveryResponse();
//                        }
//                        input.readMessage(this.deliveryResponse);
                        continue;
                    }
                    case 178: {
//                        if (this.acceptTosResponse == null) {
//                            this.acceptTosResponse = new Tos.AcceptTosResponse();
//                        }
//                        input.readMessage(this.acceptTosResponse);
                        continue;
                    }
                    case 186: {
//                        if (this.rateSuggestedContentResponse == null) {
//                            this.rateSuggestedContentResponse = new RateSuggestedContentResponse();
//                        }
//                        input.readMessage(this.rateSuggestedContentResponse);
                        continue;
                    }
                    case 194: {
//                        if (this.checkPromoOfferResponse == null) {
//                            this.checkPromoOfferResponse = new CheckPromoOffer.CheckPromoOfferResponse();
//                        }
//                        input.readMessage(this.checkPromoOfferResponse);
                        continue;
                    }
                    case 202: {
//                        if (this.instrumentSetupInfoResponse == null) {
//                            this.instrumentSetupInfoResponse = new BuyInstruments.InstrumentSetupInfoResponse();
//                        }
//                        input.readMessage(this.instrumentSetupInfoResponse);
                        continue;
                    }
                    case 210: {
//                        if (this.redeemGiftCardResponse == null) {
//                            this.redeemGiftCardResponse = new BuyInstruments.RedeemGiftCardResponse();
//                        }
//                        input.readMessage(this.redeemGiftCardResponse);
                        continue;
                    }
                    case 218: {
//                        if (this.modifyLibraryResponse == null) {
//                            this.modifyLibraryResponse = new ModifyLibrary.ModifyLibraryResponse();
//                        }
//                        input.readMessage(this.modifyLibraryResponse);
                        continue;
                    }
                    case 226: {
//                        if (this.uploadDeviceConfigResponse == null) {
//                            this.uploadDeviceConfigResponse = new UploadDeviceConfig.UploadDeviceConfigResponse();
//                        }
//                        input.readMessage(this.uploadDeviceConfigResponse);
                        continue;
                    }
                    case 234: {
//                        if (this.plusProfileResponse == null) {
//                            this.plusProfileResponse = new PlusProfile.PlusProfileResponse();
//                        }
//                        input.readMessage(this.plusProfileResponse);
                        continue;
                    }
                    case 242: {
//                        if (this.consumePurchaseResponse == null) {
//                            this.consumePurchaseResponse = new ConsumePurchaseResponse();
//                        }
//                        input.readMessage(this.consumePurchaseResponse);
                        continue;
                    }
                    case 250: {
//                        if (this.billingProfileResponse == null) {
//                            this.billingProfileResponse = new BuyInstruments.BillingProfileResponse();
//                        }
//                        input.readMessage(this.billingProfileResponse);
                        continue;
                    }
                    case 258: {
//                        if (this.preparePurchaseResponse == null) {
//                            this.preparePurchaseResponse = new Purchase.PreparePurchaseResponse();
//                        }
//                        input.readMessage(this.preparePurchaseResponse);
                        continue;
                    }
                    case 266: {
//                        if (this.commitPurchaseResponse == null) {
//                            this.commitPurchaseResponse = new Purchase.CommitPurchaseResponse();
//                        }
//                        input.readMessage(this.commitPurchaseResponse);
                        continue;
                    }
                    case 274: {
//                        if (this.debugSettingsResponse == null) {
//                            this.debugSettingsResponse = new DebugSettings.DebugSettingsResponse();
//                        }
//                        input.readMessage(this.debugSettingsResponse);
                        continue;
                    }
                    case 282: {
//                        if (this.checkIabPromoResponse == null) {
//                            this.checkIabPromoResponse = new BuyInstruments.CheckIabPromoResponse();
//                        }
//                        input.readMessage(this.checkIabPromoResponse);
                        continue;
                    }
                    case 290: {
//                        if (this.userActivitySettingsResponse == null) {
//                            this.userActivitySettingsResponse = new UserActivity.UserActivitySettingsResponse();
//                        }
//                        input.readMessage(this.userActivitySettingsResponse);
                        continue;
                    }
                    case 298: {
//                        if (this.recordUserActivityResponse == null) {
//                            this.recordUserActivityResponse = new UserActivity.RecordUserActivityResponse();
//                        }
//                        input.readMessage(this.recordUserActivityResponse);
                        continue;
                    }
                    case 306: {
//                        if (this.redeemCodeResponse == null) {
//                            this.redeemCodeResponse = new PromoCode.RedeemCodeResponse();
//                        }
//                        input.readMessage(this.redeemCodeResponse);
                        continue;
                    }
                    case 314: {
//                        if (this.selfUpdateResponse == null) {
//                            this.selfUpdateResponse = new SelfUpdate.SelfUpdateResponse();
//                        }
//                        input.readMessage(this.selfUpdateResponse);
                        continue;
                    }
                    case 322: {
//                        if (this.searchSuggestResponse == null) {
//                            this.searchSuggestResponse = new SearchSuggest.SearchSuggestResponse();
//                        }
//                        input.readMessage(this.searchSuggestResponse);
                        continue;
                    }
                    case 330: {
//                        if (this.getInitialInstrumentFlowStateResponse == null) {
//                            this.getInitialInstrumentFlowStateResponse = new BuyInstruments.GetInitialInstrumentFlowStateResponse();
//                        }
//                        input.readMessage(this.getInitialInstrumentFlowStateResponse);
                        continue;
                    }
                    case 338: {
//                        if (this.createInstrumentResponse == null) {
//                            this.createInstrumentResponse = new BuyInstruments.CreateInstrumentResponse();
//                        }
//                        input.readMessage(this.createInstrumentResponse);
                        continue;
                    }
                    case 346: {
//                        if (this.challengeResponse == null) {
//                            this.challengeResponse = new ChallengeAction.ChallengeResponse();
//                        }
//                        input.readMessage(this.challengeResponse);
                        continue;
                    }
                    case 354: {
//                        if (this.backupDeviceChoicesResponse == null) {
//                            this.backupDeviceChoicesResponse = new Restore.GetBackupDeviceChoicesResponse();
//                        }
//                        input.readMessage(this.backupDeviceChoicesResponse);
                        continue;
                    }
                    case 362: {
//                        if (this.backupDocumentChoicesResponse == null) {
//                            this.backupDocumentChoicesResponse = new Restore.GetBackupDocumentChoicesResponse();
//                        }
//                        input.readMessage(this.backupDocumentChoicesResponse);
                        continue;
                    }
                    case 370: {
//                        if (this.earlyUpdateResponse == null) {
//                            this.earlyUpdateResponse = new EarlyUpdate.EarlyUpdateResponse();
//                        }
//                        input.readMessage(this.earlyUpdateResponse);
                        continue;
                    }
                    case 378: {
//                        if (this.preloadsResponse == null) {
//                            this.preloadsResponse = new Preloads.PreloadsResponse();
//                        }
//                        input.readMessage(this.preloadsResponse);
                        continue;
                    }
                }
            }
        }

        @Override
        public void writeTo(CodedOutputByteBufferNano codedOutputByteBufferNano) throws IOException {
            if (this.listResponse != null) {
                codedOutputByteBufferNano.writeMessage(1, this.listResponse);
            }
            if (this.detailsResponse != null) {
                codedOutputByteBufferNano.writeMessage(2, this.detailsResponse);
            }
//            if (this.reviewResponse != null) {
//                codedOutputByteBufferNano.writeMessage(3, this.reviewResponse);
//            }
//            if (this.buyResponse != null) {
//                codedOutputByteBufferNano.writeMessage(4, this.buyResponse);
//            }
            if (this.searchResponse != null) {
                codedOutputByteBufferNano.writeMessage(5, this.searchResponse);
            }
//            if (this.tocResponse != null) {
//                codedOutputByteBufferNano.writeMessage(6, this.tocResponse);
//            }
//            if (this.browseResponse != null) {
//                codedOutputByteBufferNano.writeMessage(7, this.browseResponse);
//            }
//            if (this.purchaseStatusResponse != null) {
//                codedOutputByteBufferNano.writeMessage(8, this.purchaseStatusResponse);
//            }
//            if (this.updateInstrumentResponse != null) {
//                codedOutputByteBufferNano.writeMessage(9, this.updateInstrumentResponse);
//            }
//            if (this.logResponse != null) {
//                codedOutputByteBufferNano.writeMessage(10, this.logResponse);
//            }
//            if (this.checkInstrumentResponse != null) {
//                codedOutputByteBufferNano.writeMessage(11, this.checkInstrumentResponse);
//            }
//            if (this.plusOneResponse != null) {
//                codedOutputByteBufferNano.writeMessage(12, this.plusOneResponse);
//            }
//            if (this.flagContentResponse != null) {
//                codedOutputByteBufferNano.writeMessage(13, this.flagContentResponse);
//            }
//            if (this.ackNotificationResponse != null) {
//                codedOutputByteBufferNano.writeMessage(14, this.ackNotificationResponse);
//            }
//            if (this.initiateAssociationResponse != null) {
//                codedOutputByteBufferNano.writeMessage(15, this.initiateAssociationResponse);
//            }
//            if (this.verifyAssociationResponse != null) {
//                codedOutputByteBufferNano.writeMessage(16, this.verifyAssociationResponse);
//            }
//            if (this.libraryReplicationResponse != null) {
//                codedOutputByteBufferNano.writeMessage(17, this.libraryReplicationResponse);
//            }
//            if (this.revokeResponse != null) {
//                codedOutputByteBufferNano.writeMessage(18, this.revokeResponse);
//            }
            if (this.bulkDetailsResponse != null) {
                codedOutputByteBufferNano.writeMessage(19, this.bulkDetailsResponse);
            }
            if (this.resolveLinkResponse != null) {
                codedOutputByteBufferNano.writeMessage(20, this.resolveLinkResponse);
            }
//            if (this.deliveryResponse != null) {
//                codedOutputByteBufferNano.writeMessage(21, this.deliveryResponse);
//            }
//            if (this.acceptTosResponse != null) {
//                codedOutputByteBufferNano.writeMessage(22, this.acceptTosResponse);
//            }
//            if (this.rateSuggestedContentResponse != null) {
//                codedOutputByteBufferNano.writeMessage(23, this.rateSuggestedContentResponse);
//            }
//            if (this.checkPromoOfferResponse != null) {
//                codedOutputByteBufferNano.writeMessage(24, this.checkPromoOfferResponse);
//            }
//            if (this.instrumentSetupInfoResponse != null) {
//                codedOutputByteBufferNano.writeMessage(25, this.instrumentSetupInfoResponse);
//            }
//            if (this.redeemGiftCardResponse != null) {
//                codedOutputByteBufferNano.writeMessage(26, this.redeemGiftCardResponse);
//            }
//            if (this.modifyLibraryResponse != null) {
//                codedOutputByteBufferNano.writeMessage(27, this.modifyLibraryResponse);
//            }
//            if (this.uploadDeviceConfigResponse != null) {
//                codedOutputByteBufferNano.writeMessage(28, this.uploadDeviceConfigResponse);
//            }
//            if (this.plusProfileResponse != null) {
//                codedOutputByteBufferNano.writeMessage(29, this.plusProfileResponse);
//            }
//            if (this.consumePurchaseResponse != null) {
//                codedOutputByteBufferNano.writeMessage(30, this.consumePurchaseResponse);
//            }
//            if (this.billingProfileResponse != null) {
//                codedOutputByteBufferNano.writeMessage(31, this.billingProfileResponse);
//            }
//            if (this.preparePurchaseResponse != null) {
//                codedOutputByteBufferNano.writeMessage(32, this.preparePurchaseResponse);
//            }
//            if (this.commitPurchaseResponse != null) {
//                codedOutputByteBufferNano.writeMessage(33, this.commitPurchaseResponse);
//            }
//            if (this.debugSettingsResponse != null) {
//                codedOutputByteBufferNano.writeMessage(34, this.debugSettingsResponse);
//            }
//            if (this.checkIabPromoResponse != null) {
//                codedOutputByteBufferNano.writeMessage(35, this.checkIabPromoResponse);
//            }
//            if (this.userActivitySettingsResponse != null) {
//                codedOutputByteBufferNano.writeMessage(36, this.userActivitySettingsResponse);
//            }
//            if (this.recordUserActivityResponse != null) {
//                codedOutputByteBufferNano.writeMessage(37, this.recordUserActivityResponse);
//            }
//            if (this.redeemCodeResponse != null) {
//                codedOutputByteBufferNano.writeMessage(38, this.redeemCodeResponse);
//            }
//            if (this.selfUpdateResponse != null) {
//                codedOutputByteBufferNano.writeMessage(39, this.selfUpdateResponse);
//            }
//            if (this.searchSuggestResponse != null) {
//                codedOutputByteBufferNano.writeMessage(40, this.searchSuggestResponse);
//            }
//            if (this.getInitialInstrumentFlowStateResponse != null) {
//                codedOutputByteBufferNano.writeMessage(41, this.getInitialInstrumentFlowStateResponse);
//            }
//            if (this.createInstrumentResponse != null) {
//                codedOutputByteBufferNano.writeMessage(42, this.createInstrumentResponse);
//            }
//            if (this.challengeResponse != null) {
//                codedOutputByteBufferNano.writeMessage(43, this.challengeResponse);
//            }
//            if (this.backupDeviceChoicesResponse != null) {
//                codedOutputByteBufferNano.writeMessage(44, this.backupDeviceChoicesResponse);
//            }
//            if (this.backupDocumentChoicesResponse != null) {
//                codedOutputByteBufferNano.writeMessage(45, this.backupDocumentChoicesResponse);
//            }
//            if (this.earlyUpdateResponse != null) {
//                codedOutputByteBufferNano.writeMessage(46, this.earlyUpdateResponse);
//            }
//            if (this.preloadsResponse != null) {
//                codedOutputByteBufferNano.writeMessage(47, this.preloadsResponse);
//            }
            super.writeTo(codedOutputByteBufferNano);
        }
    }

    public static final class ResponseWrapper extends MessageNano {
        public ResponseMessages.ServerCommands commands;
        public Response.Payload payload;
        public ResponseMessages.PreFetch[] preFetch;
        public ResponseMessages.ServerMetadata serverMetadata;

        public ResponseWrapper() {
            clear();
        }

        public static ResponseWrapper parseFrom(byte[] paramArrayOfByte)
                throws InvalidProtocolBufferNanoException {
            return (ResponseWrapper) MessageNano.mergeFrom(new ResponseWrapper(), paramArrayOfByte);
        }

        public ResponseWrapper clear() {
            this.payload = null;
            this.commands = null;
            this.preFetch = ResponseMessages.PreFetch.emptyArray();
            this.serverMetadata = null;
            this.cachedSize = -1;
            return this;
        }

        protected int computeSerializedSize() {
            int i = super.computeSerializedSize();
            if (this.payload != null)
                i += CodedOutputByteBufferNano.computeMessageSize(1, this.payload);
            if (this.commands != null)
                i += CodedOutputByteBufferNano.computeMessageSize(2, this.commands);
            if ((this.preFetch != null) && (this.preFetch.length > 0))
                for (int k = 0; k < this.preFetch.length; k++) {
                    ResponseMessages.PreFetch localPreFetch = this.preFetch[k];
                    if (localPreFetch != null)
                        i += CodedOutputByteBufferNano.computeMessageSize(3, localPreFetch);
                }
            if (this.serverMetadata != null)
                i += CodedOutputByteBufferNano.computeMessageSize(5, this.serverMetadata);
            return i;
        }

        @Override
        public ResponseWrapper mergeFrom(CodedInputByteBufferNano codedInputByteBufferNano) throws IOException {
            int tag;
            int repeatedFieldArrayLength;
            int i;
            ResponseMessages.PreFetch[] preFetch;
            while (true) {
                tag = codedInputByteBufferNano.readTag();
                switch (tag) {
                    default: {
                        if (!WireFormatNano.parseUnknownField(codedInputByteBufferNano, tag)) {
                            return this;
                        }
                        break;
                    }
                    case 0: {
                        return this;
                    }
                    case 10: {
                        if (this.payload == null) {
                            this.payload = new Payload();
                        }
                        codedInputByteBufferNano.readMessage(this.payload);
                        continue;
                    }
                    case 18: {
                        if (this.commands == null) {
                            this.commands = new ResponseMessages.ServerCommands();
                        }
                        codedInputByteBufferNano.readMessage(this.commands);
                        continue;
                    }
                    case 26: {
                        repeatedFieldArrayLength = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 26);
                        if (this.preFetch == null) {
                            i = 0;
                        }
                        else {
                            i = this.preFetch.length;
                        }
                        preFetch = new ResponseMessages.PreFetch[i + repeatedFieldArrayLength];
                        if (i != 0) {
                            System.arraycopy(this.preFetch, 0, preFetch, 0, i);
                        }
                        while (i < -1 + preFetch.length) {
                            codedInputByteBufferNano.readMessage(preFetch[i] = new ResponseMessages.PreFetch());
                            codedInputByteBufferNano.readTag();
                            ++i;
                        }
                        codedInputByteBufferNano.readMessage(preFetch[i] = new ResponseMessages.PreFetch());
                        this.preFetch = preFetch;
                        continue;
                    }
                    case 34: {
//                        repeatedFieldArrayLength2 = WireFormatNano.getRepeatedFieldArrayLength(codedInputByteBufferNano, 34);
//                        if (this.notification == null) {
//                            j = 0;
//                        }
//                        else {
//                            j = this.notification.length;
//                        }
//                        notification = new Notifications.Notification[j + repeatedFieldArrayLength2];
//                        if (j != 0) {
//                            System.arraycopy(this.notification, 0, notification, 0, j);
//                        }
//                        while (j < -1 + notification.length) {
//                            codedInputByteBufferNano.readMessage(notification[j] = new Notifications.Notification());
//                            codedInputByteBufferNano.readTag();
//                            ++j;
//                        }
//                        codedInputByteBufferNano.readMessage(notification[j] = new Notifications.Notification());
//                        this.notification = notification;
                        continue;
                    }
                    case 42: {
                        if (this.serverMetadata == null) {
                            this.serverMetadata = new ResponseMessages.ServerMetadata();
                        }
                        codedInputByteBufferNano.readMessage(this.serverMetadata);
                        continue;
                    }
                    case 50: {
//                        if (this.targets == null) {
//                            this.targets = new Targeting.Targets();
//                        }
//                        codedInputByteBufferNano.readMessage(this.targets);
                        continue;
                    }
                }
            }
        }

        public void writeTo(CodedOutputByteBufferNano paramCodedOutputByteBufferNano)
                throws IOException {
            if (this.payload != null)
                paramCodedOutputByteBufferNano.writeMessage(1, this.payload);
            if (this.commands != null)
                paramCodedOutputByteBufferNano.writeMessage(2, this.commands);
            if ((this.preFetch != null) && (this.preFetch.length > 0))
                for (int j = 0; j < this.preFetch.length; j++) {
                    ResponseMessages.PreFetch localPreFetch = this.preFetch[j];
                    if (localPreFetch != null)
                        paramCodedOutputByteBufferNano.writeMessage(3, localPreFetch);
                }
            if (this.serverMetadata != null)
                paramCodedOutputByteBufferNano.writeMessage(5, this.serverMetadata);
            super.writeTo(paramCodedOutputByteBufferNano);
        }
    }
}
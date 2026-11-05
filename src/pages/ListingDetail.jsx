import React, { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { MapPin, Shield, Star, Share2, AlertTriangle, Package, MessageCircle, X } from 'lucide-react';
import toast from 'react-hot-toast';
import PriceReferenceWidget from '../components/dashboard/PriceReferenceWidget';
import { useAuth } from '../contexts/AuthContext';
import { getListing } from '../services/listings';
import { createOffer, getOffersForListing, getMyOffers, acceptOffer, rejectOffer } from '../services/offers';
import { createConversation } from '../services/chat';
import { toMediaUrl } from '../services/api';
import { createReview } from '../services/reviews';

const ListingDetail = () => {
  const { id } = useParams();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [listing, setListing] = useState(null);
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [offerQuantity, setOfferQuantity] = useState('');
  const [offerPrice, setOfferPrice] = useState('');
  const [rentalStart, setRentalStart] = useState('');
  const [rentalEnd, setRentalEnd] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [imagePreview, setImagePreview] = useState(false);
  const [rating, setRating] = useState(5);
  const [reviewComment, setReviewComment] = useState('');
  const [reviewingOfferId, setReviewingOfferId] = useState(null);
  const isOwner = Boolean(listing && user && String(listing.seller?._id || listing.seller?.id || listing.seller) === String(user?._id || user?.id));

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await getListing(id);
      setListing(data.data);
      setOfferPrice(String(data.data.price));
    } catch (error) { toast.error(error.response?.data?.message || t('listing.loadError', 'Could not load listing.')); }
    finally { setLoading(false); }
  }, [id, t]);

  const loadOffers = useCallback(async () => {
    if (!listing || !isAuthenticated) return;
    try {
      if (isOwner) { const { data } = await getOffersForListing(id); setOffers(data.data || []); }
      else { const { data } = await getMyOffers(); setOffers((data.data || []).filter((offer) => String(offer.listing?._id || offer.listing?.id || offer.listing) === String(id))); }
    }
    catch (error) { toast.error(error.response?.data?.message || t('offers.loadError', 'Could not load offers.')); }
  }, [id, listing, isOwner, isAuthenticated, t]);

  useEffect(() => { load(); }, [load]);
  useEffect(() => { loadOffers(); }, [loadOffers]);

  const submitOffer = async (event) => {
    event.preventDefault();
    if (!isAuthenticated) return navigate('/login');
    setSubmitting(true);
    try {
      await createOffer({ listing: id, price: Number(offerPrice), quantity: Number(offerQuantity), unit: listing.unit, rentalStart: listing.transactionType === 'rent' ? rentalStart : undefined, rentalEnd: listing.transactionType === 'rent' ? rentalEnd : undefined });
      toast.success(t('offers.sent', 'Your offer was sent to the seller.'));
      setOfferQuantity('');
    } catch (error) { toast.error(error.response?.data?.message || t('offers.submitError', 'Could not submit offer.')); }
    finally { setSubmitting(false); }
  };

  const contactSeller = async () => {
    if (!isAuthenticated) return navigate('/login');
    try {
      const { data } = await createConversation({ recipientId: listing.seller?._id || listing.seller?.id, listingId: listing._id || listing.id });
      const conversationId = data.data?.conversation?.id || data.data?.conversation?._id;
      if (!conversationId) throw new Error('No conversation id returned');
      navigate(`/chat/${conversationId}`);
    } catch (error) { toast.error(error.response?.data?.message || 'Could not start conversation. Please try again.'); }
  };

  const runOfferAction = async (action, offer) => {
    try {
      const offerId = offer._id || offer.id;
      if (action === 'accept') await acceptOffer(offerId);
      if (action === 'reject') await rejectOffer(offerId);
      toast.success(action === 'accept' ? t('offers.acceptedOrderCreated', 'Offer accepted and order created.') : t(`offers.action.${action}`, `Offer ${action}ed.`));
      await loadOffers();
      if (action === 'accept') await load();
    } catch (error) { toast.error(error.response?.data?.message || t(`offers.actionError.${action}`, `Could not ${action} offer.`)); }
  };

  const submitReview = async (offer) => {
    try {
      setReviewingOfferId(offer.id || offer._id);
      await createReview({ offerId: offer.id || offer._id, rating: Number(rating), comment: reviewComment });
      toast.success('Thank you — your seller rating was recorded.');
      setReviewComment('');
      await loadOffers();
    } catch (error) { toast.error(error.response?.data?.message || 'Could not save your rating.'); }
    finally { setReviewingOfferId(null); }
  };

  if (loading) return <div className="min-h-[50vh] grid place-items-center text-primary-700">{t('listing.loading', 'Loading listing…')}</div>;
  if (!listing) return <div className="min-h-[50vh] grid place-items-center">{t('listing.notFound', 'Listing not found.')}</div>;
  const seller = listing.seller || {};
  const location = listing.location || {};
  const locationText = [location.village, location.taluka, location.district, location.state].filter(Boolean).join(', ') || 'Location not provided';

  return (
    <div className="min-h-screen bg-earth-50 py-8"><div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="flex justify-between items-center mb-6"><p className="text-sm text-gray-500"><Link to="/marketplace">{t('nav.marketplace', 'Marketplace')}</Link> / {t(`categories.${listing.category}`, listing.category)} / <span className="text-gray-900">{listing.title}</span></p><button onClick={() => navigator.share?.({ title: listing.title, url: window.location.href })} className="flex items-center gap-2 px-4 py-2 bg-white rounded-xl border"><Share2 size={18} /> {t('common.share', 'Share')}</button></div>
      <div className="grid lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-white p-4 rounded-3xl border"><div onClick={() => listing.images?.[0] && setImagePreview(true)} className="aspect-video bg-earth-100 rounded-2xl overflow-hidden flex items-center justify-center cursor-zoom-in">{listing.images?.[0] ? <img src={toMediaUrl(listing.images[0])} alt={listing.title} className="w-full h-full object-cover" /> : <span className="text-6xl">🌾</span>}</div></div>
          <section className="bg-white p-6 sm:p-8 rounded-3xl border">
            <div className="flex gap-2 mb-3"><span className="px-3 py-1 bg-primary-100 text-primary-700 rounded-lg text-sm font-bold uppercase">{t(`listing.${listing.transactionType}`, listing.transactionType)}</span>{listing.organic && <span className="px-3 py-1 bg-green-100 text-green-700 rounded-lg text-sm font-bold">{t('listing.organic', 'Organic')}</span>}</div>
            <h1 className="text-3xl font-bold mb-5">{listing.title}</h1>
            <div className="flex flex-wrap items-end gap-5 mb-6 pb-6 border-b"><div><div className="text-4xl font-black text-accent-600">₹{listing.price}</div><div className="text-gray-500">{t('common.per', 'per')} {t(`units.${listing.unit?.toLowerCase()}`, listing.unit)}</div></div><div className="bg-earth-50 px-4 py-2 rounded-xl"><div className="text-sm text-gray-500">{t('listing.availableQuantity', 'Available quantity')}</div><div className="text-xl font-bold">{listing.quantity} {t(`units.${listing.unit?.toLowerCase()}`, listing.unit)}</div></div></div>
            <div className="flex gap-2 text-gray-600 mb-6"><MapPin className="text-primary-600" />{locationText}</div>
            <h2 className="text-xl font-bold mb-2">{t('listing.description', 'Description')}</h2><p className="text-gray-700 whitespace-pre-line">{listing.description || t('listing.noDescription', 'No description provided.')}</p>
            <div className="grid sm:grid-cols-2 gap-4 mt-6">{[[t('listing.variety', 'Variety'), listing.variety], [t('listing.harvestDate', 'Harvest date'), listing.harvestDate], [t('listing.grade', 'Grade'), listing.grade]].filter(([, value]) => value !== undefined && value !== '').map(([label, value]) => <div key={label} className="bg-earth-50 p-4 rounded-xl"><div className="text-sm text-gray-500">{label}</div><div className="font-semibold">{value}</div></div>)}</div>
          </section>
          {isAuthenticated && offers.length > 0 && <section className="bg-white p-6 rounded-3xl border"><h2 className="text-xl font-bold mb-4">{isOwner ? `Offers received (${offers.length})` : 'Your offers on this listing'}</h2><div className="space-y-3">{offers.map((offer) => <div key={offer.id || offer._id} className="border rounded-xl p-4"><div className="flex flex-wrap items-center justify-between gap-3"><div><div className="font-semibold">{isOwner ? offer.buyer?.name || 'Buyer' : 'Your offer'} · ₹{offer.price}/{offer.unit} · {offer.quantity} {offer.unit}</div><div className="text-sm text-gray-500 capitalize">{offer.status}</div></div>{isOwner && offer.status === 'pending' && <div className="flex gap-2"><button onClick={() => runOfferAction('accept', offer)} className="px-3 py-2 rounded-lg bg-green-600 text-white">Accept</button><button onClick={() => runOfferAction('reject', offer)} className="px-3 py-2 rounded-lg border text-red-600">Decline</button></div>}</div>{!isOwner && offer.status === 'accepted' && <form onSubmit={(event) => { event.preventDefault(); submitReview(offer); }} className="mt-4 border-t pt-4"><p className="font-semibold mb-2">Rate this seller</p><select value={rating} onChange={(event) => setRating(event.target.value)} className="border rounded-lg p-2 mr-2">{[5,4,3,2,1].map(value => <option key={value} value={value}>{value} star{value > 1 ? 's' : ''}</option>)}</select><input value={reviewComment} onChange={(event) => setReviewComment(event.target.value)} placeholder="Optional review" className="border rounded-lg p-2 mr-2"/><button disabled={reviewingOfferId === (offer.id || offer._id)} className="bg-primary-600 text-white rounded-lg px-3 py-2">{reviewingOfferId === (offer.id || offer._id) ? 'Saving…' : 'Submit rating'}</button></form>}</div>)}</div></section>}
        </div>
        <aside className="space-y-6">
          <PriceReferenceWidget category={listing.category} location={[location.state, location.district].filter(Boolean).join(',')} currentPrice={listing.price} unit={listing.unit} commodity={listing.title} />
          <section className="bg-white p-6 rounded-3xl border"><h2 className="font-bold mb-4">{t('listing.sellerInformation', 'Seller information')}</h2><div className="flex items-center gap-3 mb-5"><div className="w-12 h-12 rounded-full bg-primary-100 flex items-center justify-center text-primary-700 font-bold">{seller.name?.[0] || '?'}</div><div><div className="font-semibold flex items-center gap-1">{seller.name || t('orders.seller', 'Seller')}{seller.verifiedIdentity && <Shield size={15} className="text-green-600" />}</div><div className="flex items-center gap-1 text-sm text-gray-500"><Star size={14} className="text-yellow-500" />{seller.rating || t('profile.newSeller', 'New seller')}</div></div></div><button onClick={contactSeller} disabled={isOwner} className="w-full border-2 border-primary-600 text-primary-700 rounded-xl px-4 py-3 font-bold disabled:opacity-50"><MessageCircle className="inline mr-2" size={18} />{t('listing.contactSeller', 'Contact Seller')}</button></section>
          {!isOwner && listing.status === 'active' && <form onSubmit={submitOffer} className="bg-white p-6 rounded-3xl border border-t-4 border-t-accent-500"><h2 className="text-xl font-bold mb-2">{listing.transactionType === 'rent' ? 'Request rental' : t('offers.make_an_offer', 'Make an offer')}</h2><label className="block text-sm font-semibold mb-1">{t('common.quantity', 'Quantity')} ({t(`units.${listing.unit?.toLowerCase()}`, listing.unit)})</label><input required min="0.01" max={listing.quantity} step="any" type="number" value={offerQuantity} onChange={(e) => setOfferQuantity(e.target.value)} className="w-full mb-3 border rounded-xl p-3"/>{listing.transactionType === 'rent' && <div className="grid grid-cols-2 gap-2 mb-3"><div><label className="text-sm font-semibold">Start date</label><input required type="date" value={rentalStart} onChange={e=>setRentalStart(e.target.value)} className="w-full border rounded-xl p-3"/></div><div><label className="text-sm font-semibold">End date</label><input required type="date" min={rentalStart} value={rentalEnd} onChange={e=>setRentalEnd(e.target.value)} className="w-full border rounded-xl p-3"/></div></div>}<label className="block text-sm font-semibold mb-1">{t('common.per', 'Price per')} {t(`units.${listing.unit?.toLowerCase()}`, listing.unit)} (₹)</label><input required min="0.01" step="any" type="number" value={offerPrice} onChange={(e) => setOfferPrice(e.target.value)} className="w-full mb-4 border rounded-xl p-3"/><button disabled={submitting} className="w-full rounded-xl p-3 font-bold bg-accent-600 text-white disabled:opacity-50">{submitting ? t('offers.sending', 'Sending…') : t('offers.submit_offer', 'Submit Offer')}</button></form>}
        </aside>
      </div>
    {imagePreview && <div className="fixed inset-0 z-50 bg-black/90 flex items-center justify-center p-4" onClick={() => setImagePreview(false)}><button className="absolute top-5 right-5 text-white"><X size={32}/></button><img src={toMediaUrl(listing.images[0])} alt={listing.title} className="max-h-[95vh] max-w-[95vw] object-contain" /></div>}</div></div>
  );
};

export default ListingDetail;

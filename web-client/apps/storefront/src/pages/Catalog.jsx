import { useState, useMemo } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { 
  Typography, 
  Button, 
  Box, 
  CircularProgress, 
  Alert, 
  Grid, 
  Paper, 
  TextField, 
  InputAdornment, 
  IconButton, 
  FormControl, 
  InputLabel, 
  Select, 
  MenuItem, 
  Slider, 
  Drawer, 
  List, 
  ListItem, 
  ListItemButton, 
  ListItemText, 
  Collapse,
  Card,
  CardMedia,
  CardContent
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ClearIcon from '@mui/icons-material/Clear';
import TuneIcon from '@mui/icons-material/Tune';
import CloseIcon from '@mui/icons-material/Close';
import RestartAltIcon from '@mui/icons-material/RestartAlt';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import api from '../services/api';

const getPremiumCarImage = (brand, bodyType) => {
  const images = {
    BMW: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80',
    AUDI: 'https://images.unsplash.com/photo-1606016159991-dfe4f974be5c?auto=format&fit=crop&w=800&q=80',
    MERCEDES_BENZ: 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80',
    TESLA: 'https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&w=800&q=80',
    TOYOTA: 'https://images.unsplash.com/photo-1532581291347-9c39cf10a73c?auto=format&fit=crop&w=800&q=80',
    SUV: 'https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=800&q=80',
    SEDAN: 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80',
    COUPE: 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?auto=format&fit=crop&w=800&q=80',
    CONVERTIBLE: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80'
  };
  return images[brand] || images[bodyType] || 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80';
};

const fetchCars = async () => {
  try {
    const res = await api.post('/v1/catalog/cars/filter', {});
    return res.data;
  } catch (error) {
    // Return mock data for local testing if backend is down
    return [
      { id: '1', name: 'Sedan X', price: 45000, description: 'Luxury sedan with premium features', imageUrl: 'https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=800&q=80', brand: 'BMW', bodyType: 'SEDAN', enginePower: 250, engineVolumeLiters: 2.0, gearBoxType: 'AUTOMATIC', drivetrainType: 'RWD', color: 'BLACK', fuelType: 'GASOLINE', testDrive: true },
      { id: '2', name: 'SUV Y', price: 65000, description: 'Spacious family SUV', imageUrl: 'https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?auto=format&fit=crop&w=800&q=80', brand: 'AUDI', bodyType: 'SUV', enginePower: 300, engineVolumeLiters: 3.0, gearBoxType: 'AUTOMATIC', drivetrainType: 'AWD', color: 'WHITE', fuelType: 'DIESEL', testDrive: false },
      { id: '3', name: 'Sports Z', price: 85000, description: 'High performance sports car', imageUrl: 'https://images.unsplash.com/photo-1583121274602-3e2820c69888?auto=format&fit=crop&w=800&q=80', brand: 'TESLA', bodyType: 'COUPE', enginePower: 450, engineVolumeLiters: 0.0, gearBoxType: 'AUTOMATIC', drivetrainType: 'AWD', color: 'RED', fuelType: 'ELECTRIC', testDrive: true },
      { id: '4', name: 'Sedan Classic', price: 38000, description: 'Elegant daily sedan', imageUrl: 'https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&w=800&q=80', brand: 'BMW', bodyType: 'SEDAN', enginePower: 184, engineVolumeLiters: 2.0, gearBoxType: 'AUTOMATIC', drivetrainType: 'RWD', color: 'BLUE', fuelType: 'GASOLINE', testDrive: false },
      { id: '5', name: 'Roadster Speedster', price: 120000, description: 'Open top sports dynamic roadster', imageUrl: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=800&q=80', brand: 'OTHER', bodyType: 'CONVERTIBLE', enginePower: 500, engineVolumeLiters: 4.0, gearBoxType: 'MANUAL', drivetrainType: 'RWD', color: 'YELLOW', fuelType: 'GASOLINE', testDrive: true },
      { id: '6', name: 'Luxury Cruiser', price: 95000, description: 'Top of the line executive sedan', imageUrl: 'https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?auto=format&fit=crop&w=800&q=80', brand: 'MERCEDES_BENZ', bodyType: 'SEDAN', enginePower: 367, engineVolumeLiters: 3.0, gearBoxType: 'AUTOMATIC', drivetrainType: 'AWD', color: 'BLACK', fuelType: 'HYBRID', testDrive: false }
    ];
  }
};

const fetchModels = async () => {
  try {
    const res = await api.get('/v1/catalog/models');
    return res.data;
  } catch (error) {
    return [];
  }
};

export default function Catalog() {
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [selectedBrand, setSelectedBrand] = useState('ALL');
  const [priceRange, setPriceRange] = useState([0, 150000]);
  const [sortBy, setSortBy] = useState('price_asc');
  const [mobileOpen, setMobileOpen] = useState(false);

  const [catExpanded, setCatExpanded] = useState(true);
  const [brandExpanded, setBrandExpanded] = useState(true);
  const [priceExpanded, setPriceExpanded] = useState(true);

  const { data: carsData, isLoading: isCarsLoading, error: carsError } = useQuery({
    queryKey: ['cars'],
    queryFn: fetchCars,
  });

  const { data: modelsData, isLoading: isModelsLoading } = useQuery({
    queryKey: ['models'],
    queryFn: fetchModels,
  });

  const addToCartMutation = useMutation({
    mutationFn: async (car) => {
      const payload = {
        items: [{
          carId: car.id,
          quantity: 1,
          price: car.price
        }]
      };
      await api.post('/v1/cart', payload);
    },
    onSuccess: () => {
      alert('Added to cart!');
    },
    onError: () => {
      alert('Failed to add to cart. Mocking success for MVP.');
    }
  });

  const categories = [
    { value: 'ALL', label: 'All Categories' },
    { value: 'SEDAN', label: 'Sedan' },
    { value: 'SUV', label: 'SUV' },
    { value: 'COUPE', label: 'Coupe' },
    { value: 'HATCHBACK', label: 'Hatchback' },
    { value: 'CONVERTIBLE', label: 'Convertible' },
    { value: 'OTHER', label: 'Other' },
  ];

  const brands = [
    { value: 'ALL', label: 'All Brands' },
    { value: 'BMW', label: 'BMW' },
    { value: 'AUDI', label: 'Audi' },
    { value: 'MERCEDES_BENZ', label: 'Mercedes-Benz' },
    { value: 'TOYOTA', label: 'Toyota' },
    { value: 'HONDA', label: 'Honda' },
    { value: 'VOLKSWAGEN', label: 'Volkswagen' },
    { value: 'FORD', label: 'Ford' },
    { value: 'TESLA', label: 'Tesla' },
    { value: 'OTHER', label: 'Other' },
  ];

  const handlePriceChange = (event, newValue) => {
    setPriceRange(newValue);
  };

  const handleResetFilters = () => {
    setSearch('');
    setSelectedCategory('ALL');
    setSelectedBrand('ALL');
    setPriceRange([0, 150000]);
  };

  const categoryCounts = useMemo(() => {
    const counts = {};
    if (!carsData) return counts;
    carsData.forEach(car => {
      const model = modelsData?.find(m => m.id === car.modelId);
      const type = car.bodyType || model?.bodyType || 'OTHER';
      counts[type] = (counts[type] || 0) + 1;
    });
    return counts;
  }, [carsData, modelsData]);

  const brandCounts = useMemo(() => {
    const counts = {};
    if (!carsData) return counts;
    carsData.forEach(car => {
      const model = modelsData?.find(m => m.id === car.modelId);
      const brand = car.brand || model?.brand || 'OTHER';
      counts[brand] = (counts[brand] || 0) + 1;
    });
    return counts;
  }, [carsData, modelsData]);

  const getCategoryCount = (cat) => {
    if (cat === 'ALL') return carsData?.length || 0;
    return categoryCounts[cat] || 0;
  };

  const getBrandCount = (brand) => {
    if (brand === 'ALL') return carsData?.length || 0;
    return brandCounts[brand] || 0;
  };

  const filteredCars = useMemo(() => {
    if (!carsData) return [];
    
    const mapped = carsData.map(car => {
      const model = modelsData?.find(m => m.id === car.modelId);
      
      const brand = car.brand || model?.brand || 'OTHER';
      const bodyType = car.bodyType || model?.bodyType || 'OTHER';
      const name = car.name || (model ? `${brand} ${model.modelName}` : `Vehicle #${String(car.id).slice(0, 4)}`);
      const fuelType = car.fuelType || model?.fuelType || 'GASOLINE';
      const enginePower = car.enginePower || model?.enginePower || 180;
      const engineVolume = car.engineVolumeLiters || model?.engineVolumeLiters || 2.0;
      const gearbox = car.gearbox || model?.gearBoxType || 'AUTOMATIC';
      const drivetrain = car.drivetrain || model?.drivetrainType || 'RWD';
      const colorName = car.color || 'BLACK';
      const desc = car.description || `${brand} ${model?.modelName || ''} finished in stunning ${colorName.toLowerCase()}. Engineered for superior comfort and performance.`;
      const imageUrl = car.imageUrl || getPremiumCarImage(brand, bodyType);

      return {
        ...car,
        name,
        brand,
        bodyType,
        fuelType,
        enginePower,
        engineVolume,
        gearbox,
        drivetrain,
        description: desc,
        imageUrl
      };
    });

    return mapped.filter(car => {
      const searchStr = `${car.name} ${car.brand} ${car.bodyType} ${car.description} ${car.color}`.toLowerCase();
      const matchesSearch = searchStr.includes(search.toLowerCase());
      const matchesCategory = selectedCategory === 'ALL' || car.bodyType === selectedCategory;
      const matchesBrand = selectedBrand === 'ALL' || car.brand === selectedBrand;
      const matchesPrice = car.price >= priceRange[0] && car.price <= priceRange[1];

      return matchesSearch && matchesCategory && matchesBrand && matchesPrice;
    }).sort((a, b) => {
      if (sortBy === 'price_asc') return a.price - b.price;
      if (sortBy === 'price_desc') return b.price - a.price;
      if (sortBy === 'name_asc') return a.name.localeCompare(b.name);
      if (sortBy === 'name_desc') return b.name.localeCompare(a.name);
      return 0;
    });
  }, [carsData, modelsData, search, selectedCategory, selectedBrand, priceRange, sortBy]);

  const renderFilters = () => (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h6" fontWeight="bold" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TuneIcon color="primary" /> Filters
        </Typography>
        {(selectedCategory !== 'ALL' || selectedBrand !== 'ALL' || priceRange[0] !== 0 || priceRange[1] !== 150000 || search !== '') && (
          <Button size="small" onClick={handleResetFilters} startIcon={<RestartAltIcon />} sx={{ minWidth: 'auto', p: 0.5 }}>
            Reset
          </Button>
        )}
      </Box>

      {/* Category Filter */}
      <Box sx={{ mb: 2 }}>
        <Box 
          onClick={() => setCatExpanded(!catExpanded)} 
          sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', py: 1 }}
        >
          <Typography variant="subtitle2" fontWeight="bold">Category</Typography>
          {catExpanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
        </Box>
        <Collapse in={catExpanded}>
          <List size="small" disablePadding sx={{ mt: 1 }}>
            {categories.map((cat) => {
              const count = getCategoryCount(cat.value);
              const isSelected = selectedCategory === cat.value;
              return (
                <ListItem key={cat.value} disablePadding>
                  <ListItemButton 
                    onClick={() => setSelectedCategory(cat.value)}
                    sx={{ 
                      borderRadius: 2,
                      py: 0.3,
                      px: 1.2,
                      mb: 0.5,
                      bgcolor: isSelected ? 'rgba(212, 175, 55, 0.12)' : 'transparent',
                      border: isSelected ? '1px solid rgba(212, 175, 55, 0.3)' : '1px solid transparent',
                      '&:hover': { bgcolor: isSelected ? 'rgba(212, 175, 55, 0.18)' : 'rgba(255, 255, 255, 0.05)' }
                    }}
                  >
                    <ListItemText 
                      primary={cat.label} 
                      primaryTypographyProps={{ 
                        variant: 'body2', 
                        fontWeight: isSelected ? 'bold' : 'normal',
                        color: isSelected ? 'primary.main' : 'text.primary'
                      }} 
                    />
                    {count > 0 && (
                      <Typography variant="caption" color="text.secondary" sx={{ opacity: 0.8 }}>
                        ({count})
                      </Typography>
                    )}
                  </ListItemButton>
                </ListItem>
              );
            })}
          </List>
        </Collapse>
      </Box>

      {/* Brand Filter */}
      <Box sx={{ mb: 2 }}>
        <Box 
          onClick={() => setBrandExpanded(!brandExpanded)} 
          sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', py: 1 }}
        >
          <Typography variant="subtitle2" fontWeight="bold">Brand</Typography>
          {brandExpanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
        </Box>
        <Collapse in={brandExpanded}>
          <List size="small" disablePadding sx={{ mt: 1 }}>
            {brands.map((b) => {
              const count = getBrandCount(b.value);
              const isSelected = selectedBrand === b.value;
              return (
                <ListItem key={b.value} disablePadding>
                  <ListItemButton 
                    onClick={() => setSelectedBrand(b.value)}
                    sx={{ 
                      borderRadius: 2,
                      py: 0.3,
                      px: 1.2,
                      mb: 0.5,
                      bgcolor: isSelected ? 'rgba(212, 175, 55, 0.12)' : 'transparent',
                      border: isSelected ? '1px solid rgba(212, 175, 55, 0.3)' : '1px solid transparent',
                      '&:hover': { bgcolor: isSelected ? 'rgba(212, 175, 55, 0.18)' : 'rgba(255, 255, 255, 0.05)' }
                    }}
                  >
                    <ListItemText 
                      primary={b.label} 
                      primaryTypographyProps={{ 
                        variant: 'body2', 
                        fontWeight: isSelected ? 'bold' : 'normal',
                        color: isSelected ? 'primary.main' : 'text.primary'
                      }} 
                    />
                    {count > 0 && (
                      <Typography variant="caption" color="text.secondary" sx={{ opacity: 0.8 }}>
                        ({count})
                      </Typography>
                    )}
                  </ListItemButton>
                </ListItem>
              );
            })}
          </List>
        </Collapse>
      </Box>

      {/* Price Slider */}
      <Box>
        <Box 
          onClick={() => setPriceExpanded(!priceExpanded)} 
          sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer', py: 1 }}
        >
          <Typography variant="subtitle2" fontWeight="bold">Price Range</Typography>
          {priceExpanded ? <ExpandLessIcon fontSize="small" /> : <ExpandMoreIcon fontSize="small" />}
        </Box>
        <Collapse in={priceExpanded}>
          <Box sx={{ px: 1, pt: 2 }}>
            <Slider
              value={priceRange}
              onChange={handlePriceChange}
              valueLabelDisplay="auto"
              min={0}
              max={150000}
              step={5000}
              valueLabelFormat={(val) => `$${val.toLocaleString()}`}
              color="primary"
            />
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 1 }}>
              <Typography variant="caption" color="text.secondary">
                Min: ${priceRange[0].toLocaleString()}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                Max: ${priceRange[1].toLocaleString()}
              </Typography>
            </Box>
          </Box>
        </Collapse>
      </Box>
    </Box>
  );

  const renderCarCard = (car) => (
    <Card sx={{ height: '100%', display: 'flex', flexDirection: 'column', width: '100%' }}>
      <Box sx={{ position: 'relative', overflow: 'hidden', height: 240 }}>
        <CardMedia
          component="img"
          image={car.imageUrl}
          alt={car.name}
          sx={{
            height: '100%',
            width: '100%',
            objectFit: 'cover',
            transition: 'transform 0.5s ease',
            '&:hover': {
              transform: 'scale(1.05)',
            }
          }}
        />
        {car.testDrive && (
          <Box sx={{
            position: 'absolute',
            top: 12,
            right: 12,
            bgcolor: 'rgba(212, 175, 55, 0.95)',
            color: '#000000',
            px: 1.5,
            py: 0.5,
            borderRadius: 10,
            fontSize: '0.72rem',
            fontWeight: 'bold',
            backdropFilter: 'blur(5px)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.25)'
          }}>
            Test Drive
          </Box>
        )}
      </Box>
      <CardContent sx={{ flexGrow: 1, p: 3, display: 'flex', flexDirection: 'column' }}>
        <Typography variant="h5" component="h2" fontWeight="bold" gutterBottom sx={{ fontSize: '1.25rem' }}>
          {car.name}
        </Typography>
        
        {/* Specifications Pills */}
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.8, my: 1.5 }}>
          {car.engineVolume > 0 && (
            <Box sx={{ px: 1.2, py: 0.4, borderRadius: 10, fontSize: '0.72rem', bgcolor: (theme) => theme.palette.mode === 'light' ? 'rgba(0,0,0,0.05)' : 'rgba(255,255,255,0.06)', border: (theme) => `1px solid ${theme.palette.divider}`, color: 'text.secondary', display: 'flex', alignItems: 'center' }}>
              {car.engineVolume}L
            </Box>
          )}
          {car.enginePower > 0 && (
            <Box sx={{ px: 1.2, py: 0.4, borderRadius: 10, fontSize: '0.72rem', bgcolor: (theme) => theme.palette.mode === 'light' ? 'rgba(0,0,0,0.05)' : 'rgba(255,255,255,0.06)', border: (theme) => `1px solid ${theme.palette.divider}`, color: 'text.secondary', display: 'flex', alignItems: 'center' }}>
              {car.enginePower} HP
            </Box>
          )}
          <Box sx={{ px: 1.2, py: 0.4, borderRadius: 10, fontSize: '0.72rem', bgcolor: (theme) => theme.palette.mode === 'light' ? 'rgba(0,0,0,0.05)' : 'rgba(255,255,255,0.06)', border: (theme) => `1px solid ${theme.palette.divider}`, color: 'text.secondary', display: 'flex', alignItems: 'center', textTransform: 'capitalize' }}>
            {car.drivetrain.toLowerCase()}
          </Box>
          <Box sx={{ px: 1.2, py: 0.4, borderRadius: 10, fontSize: '0.72rem', bgcolor: (theme) => theme.palette.mode === 'light' ? 'rgba(0,0,0,0.05)' : 'rgba(255,255,255,0.06)', border: (theme) => `1px solid ${theme.palette.divider}`, color: 'text.secondary', display: 'flex', alignItems: 'center', textTransform: 'capitalize' }}>
            {car.gearbox.toLowerCase()}
          </Box>
        </Box>

        <Typography variant="h6" color="primary.main" fontWeight="bold" sx={{ mt: 'auto', mb: 1, fontSize: '1.25rem' }}>
          ${car.price?.toLocaleString()}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{
          display: '-webkit-box',
          WebkitLineClamp: 2,
          WebkitBoxOrient: 'vertical',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          lineHeight: 1.5,
          minHeight: '3em'
        }}>
          {car.description}
        </Typography>
      </CardContent>
      <Box sx={{ p: 3, pt: 0, display: 'flex', gap: 1.5 }}>
        <Button variant="outlined" fullWidth color="secondary" size="medium" sx={{ borderRadius: 3 }}>
          Details
        </Button>
        <Button 
          variant="contained" 
          fullWidth 
          color="primary" 
          size="medium" 
          sx={{ borderRadius: 3 }}
          onClick={() => addToCartMutation.mutate(car)}
          disabled={addToCartMutation.isPending}
        >
          {addToCartMutation.isPending ? 'Adding...' : 'Add to Cart'}
        </Button>
      </Box>
    </Card>
  );

  if (isCarsLoading || isModelsLoading) {
    return <Box textAlign="center" mt={10}><CircularProgress /></Box>;
  }

  if (carsError) {
    return (
      <Container sx={{ py: 8 }}>
        <Alert severity="error">Failed to load catalog data from server.</Alert>
      </Container>
    );
  }

  return (
    <Box sx={{ py: 6, px: { xs: 3, md: 6, lg: 8 } }}>
      <Typography variant="h3" fontWeight="bold" gutterBottom textAlign="center" mb={6}>
        Available Vehicles
      </Typography>

      {/* Main Layout CSS Grid - Sidebar is exactly 220px on desktop */}
      <Box sx={{ 
        display: 'grid', 
        gridTemplateColumns: { xs: '1fr', md: '220px 1fr' }, 
        gap: 3 
      }}>
        {/* Desktop Sidebar (Floating Glass Sheet) */}
        <Box sx={{ display: { xs: 'none', md: 'block' } }}>
          <Paper sx={{ p: 2, position: 'sticky', top: 96, maxHeight: 'calc(100vh - 120px)', overflowY: 'auto' }}>
            {renderFilters()}
          </Paper>
        </Box>

        {/* Results Column */}
        <Box sx={{ minWidth: 0 }}>
          {/* Top Search & Filter Control Panel */}
          <Box sx={{ 
            display: 'flex', 
            flexDirection: { xs: 'column', sm: 'row' }, 
            gap: 2, 
            mb: 4 
          }}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Search vehicles by name, brand, spec or color..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
                endAdornment: search && (
                  <InputAdornment position="end">
                    <IconButton onClick={() => setSearch('')} size="small">
                      <ClearIcon />
                    </IconButton>
                  </InputAdornment>
                )
              }}
              sx={{ flexGrow: 1 }}
            />
            <Box sx={{ display: 'flex', gap: 2, minWidth: { xs: '100%', sm: '320px', md: '240px' } }}>
              <FormControl fullWidth variant="outlined">
                <InputLabel id="sort-select-label">Sort By</InputLabel>
                <Select
                  labelId="sort-select-label"
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  label="Sort By"
                >
                  <MenuItem value="price_asc">Price: Low to High</MenuItem>
                  <MenuItem value="price_desc">Price: High to Low</MenuItem>
                  <MenuItem value="name_asc">Name: A to Z</MenuItem>
                  <MenuItem value="name_desc">Name: Z to A</MenuItem>
                </Select>
              </FormControl>
              <Button 
                variant="outlined" 
                onClick={() => setMobileOpen(true)}
                startIcon={<TuneIcon />}
                sx={{ display: { xs: 'flex', md: 'none' }, height: 56, px: 3, borderRadius: 4 }}
              >
                Filter
              </Button>
            </Box>
          </Box>

          <Box sx={{ mb: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Showing {filteredCars.length} of {carsData?.length || 0} vehicles
            </Typography>
          </Box>
          
          {filteredCars.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 8 }}>
              <Typography variant="h5" color="text.secondary" gutterBottom>
                No vehicles match your search or filter criteria.
              </Typography>
              <Button variant="outlined" color="primary" onClick={handleResetFilters} sx={{ mt: 2 }}>
                Clear All Filters
              </Button>
            </Box>
          ) : (
            <Box sx={{ 
              display: 'grid', 
              gridTemplateColumns: { 
                xs: '1fr', 
                sm: 'repeat(2, 1fr)', 
                lg: 'repeat(3, 1fr)' 
              }, 
              gap: 3 
            }}>
              {filteredCars.map((car) => (
                <Box key={car.id} sx={{ display: 'flex' }}>
                  {renderCarCard(car)}
                </Box>
              ))}
            </Box>
          )}
        </Box>
      </Box>

      {/* Mobile Drawer Filter (iOS 26 Floating Drawer) */}
      <Drawer
        anchor="left"
        open={mobileOpen}
        onClose={() => setMobileOpen(false)}
        PaperProps={{
          sx: {
            width: 290,
            p: 3,
            backdropFilter: 'blur(30px) saturate(210%)',
            backgroundColor: (theme) => theme.palette.mode === 'light' ? 'rgba(255, 255, 255, 0.7)' : 'rgba(18, 18, 22, 0.8)',
          }
        }}
      >
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
          <IconButton onClick={() => setMobileOpen(false)}>
            <CloseIcon />
          </IconButton>
        </Box>
        {renderFilters()}
      </Drawer>
    </Box>
  );
}

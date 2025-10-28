import React from 'react';
import { 
  Grid, 
  Paper, 
  Typography, 
  Box, 
  Card, 
  CardContent, 
  CardHeader,
  Divider,
  List,
  ListItem,
  ListItemText
} from '@mui/material';
import Layout from '../components/Layout';

const Dashboard = () => {
  // Sample data for dashboard
  const stats = [
    { label: 'Total Users', value: '12,495' },
    { label: 'Active Ads', value: '3' },
    { label: 'Total Broadcasts', value: '27' },
    { label: 'App Installs', value: '45,823' }
  ];

  // Sample recent activities
  const recentActivities = [
    { action: 'New ad campaign created', timestamp: '2 hours ago' },
    { action: 'Broadcast sent to all users', timestamp: '1 day ago' },
    { action: 'Ad statistics updated', timestamp: '2 days ago' },
    { action: 'New version released', timestamp: '5 days ago' }
  ];

  // Sample app info
  const appInfo = {
    version: '1.2.5',
    lastUpdate: '2023-11-15',
    platform: 'Android',
    status: 'Active'
  };

  return (
    <Layout>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Dashboard
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Overview of Vipul Player admin stats and activities.
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Stats Cards */}
        {stats.map((stat) => (
          <Grid item xs={12} sm={6} md={3} key={stat.label}>
            <Paper
              sx={{
                p: 2,
                display: 'flex',
                flexDirection: 'column',
                height: 120,
                justifyContent: 'center',
                alignItems: 'center',
                backgroundColor: 'primary.light',
                color: 'white'
              }}
              elevation={3}
            >
              <Typography variant="h3" component="div">
                {stat.value}
              </Typography>
              <Typography variant="subtitle1">
                {stat.label}
              </Typography>
            </Paper>
          </Grid>
        ))}

        {/* Recent Activities */}
        <Grid item xs={12} md={6}>
          <Card elevation={2}>
            <CardHeader title="Recent Activities" />
            <Divider />
            <CardContent>
              <List>
                {recentActivities.map((activity, index) => (
                  <React.Fragment key={index}>
                    <ListItem alignItems="flex-start">
                      <ListItemText
                        primary={activity.action}
                        secondary={activity.timestamp}
                      />
                    </ListItem>
                    {index < recentActivities.length - 1 && <Divider />}
                  </React.Fragment>
                ))}
              </List>
            </CardContent>
          </Card>
        </Grid>

        {/* App Info */}
        <Grid item xs={12} md={6}>
          <Card elevation={2}>
            <CardHeader title="App Information" />
            <Divider />
            <CardContent>
              <Box sx={{ mb: 2 }}>
                <Grid container spacing={2}>
                  <Grid item xs={4}>
                    <Typography variant="body2" color="text.secondary">
                      Version
                    </Typography>
                    <Typography variant="body1">
                      {appInfo.version}
                    </Typography>
                  </Grid>
                  <Grid item xs={8}>
                    <Typography variant="body2" color="text.secondary">
                      Last Update
                    </Typography>
                    <Typography variant="body1">
                      {appInfo.lastUpdate}
                    </Typography>
                  </Grid>
                  <Grid item xs={4}>
                    <Typography variant="body2" color="text.secondary">
                      Platform
                    </Typography>
                    <Typography variant="body1">
                      {appInfo.platform}
                    </Typography>
                  </Grid>
                  <Grid item xs={8}>
                    <Typography variant="body2" color="text.secondary">
                      Status
                    </Typography>
                    <Typography variant="body1" sx={{ color: 'success.main' }}>
                      {appInfo.status}
                    </Typography>
                  </Grid>
                </Grid>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Layout>
  );
};

export default Dashboard; 
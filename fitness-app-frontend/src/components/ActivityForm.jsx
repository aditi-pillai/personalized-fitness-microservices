import { Box, Button, FormControl, InputLabel, MenuItem, Select, TextField } from '@mui/material'
import React, { useState } from 'react'
import { addActivity } from '../services/api'

const ActivityForm = ({ onActivityAdded = () => {} }) => {
    const [activity, setActivity] = useState({
        activityType: "RUNNING",
        duration: '',
        caloriesBurnt: '',
        additionalMetrics: {}
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await addActivity(activity);
            onActivityAdded();
            setActivity({
                activityType: "RUNNING",
                duration: '',
                caloriesBurnt: '',
                additionalMetrics: {}
            });
        } catch (error) {
            console.error(error);
        }
    }

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ mb: 4 }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>Activity Type</InputLabel>
                <Select
                    value={activity.activityType}
                    onChange={(e) => setActivity({ ...activity, activityType: e.target.value })}
                    label="Activity Type"
                >
                    <MenuItem value="RUNNING">Running</MenuItem>
                    <MenuItem value="WALKING">Walking</MenuItem>
                    <MenuItem value="CYCLING">Cycling</MenuItem>
                    <MenuItem value="PILATES">Pilates</MenuItem>
                    <MenuItem value="WEIGHT_TRAINING">Weight Training</MenuItem>
                    <MenuItem value="CARDIO">Cardio</MenuItem>
                    <MenuItem value="YOGA">Yoga</MenuItem>
                    <MenuItem value="HIIT">HIIT</MenuItem>
                    <MenuItem value="STRETCHING">Stretching</MenuItem>
                    <MenuItem value="SWIMMING">Swimming</MenuItem>
                </Select>
            </FormControl>
            <TextField
                fullWidth
                label="Duration (Minutes)"
                type="number"
                sx={{ mb: 2 }}
                value={activity.duration}
                onChange={(e) => setActivity({ ...activity, duration: e.target.value })}
            />
            <TextField
                fullWidth
                label="Calories Burnt"
                type="number"
                sx={{ mb: 2 }}
                value={activity.caloriesBurnt}
                onChange={(e) => setActivity({ ...activity, caloriesBurnt: e.target.value })}
            />
            <Button type="submit" variant="contained">
                Add Activity
            </Button>
        </Box>
    )
}

export default ActivityForm

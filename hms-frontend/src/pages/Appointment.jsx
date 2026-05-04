import React, { useContext, useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { AppContext } from '../context/AppContext'
import { assets } from '../assets/assets'
import RelatedDoctors from '../components/RelatedDoctors'
import { toast } from 'react-toastify'
import { motion } from 'framer-motion'

const Appointment = () => {

    const { docId } = useParams()
    const { doctors, currencySymbol, api, user } = useContext(AppContext)

    const daysOfWeek = ['SUN', 'MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT']

    const [docInfo, setDocInfo] = useState(null)
    const [docSlots, setDocSlots] = useState([])
    const [slotIndex, setSlotIndex] = useState(0)
    const [slotTime, setSlotTime] = useState('')
    const [bookedSlots, setBookedSlots] = useState([])

    // Patient Info State
    const [patientInfo, setPatientInfo] = useState({
        age: user?.age || '',
        gender: user?.gender || '',
        phone: user?.phone || ''
    })

    const navigate = useNavigate()

    // Update patient info if user data changes
    useEffect(() => {
        if (user) {
            setPatientInfo({
                age: user.age || '',
                gender: user.gender || '',
                phone: user.phone || ''
            })
        }
    }, [user])

    // ✅ Get doctor info from local context
    const fetchDocInfo = () => {
        const info = doctors.find(doc => String(doc.id) === String(docId))
        setDocInfo(info)
    }

    // ✅ Fetch Booked Slots for a specific date
    const fetchBookedSlots = async (date) => {
        try {
            const dateStr = date.toISOString().split('T')[0]
            const { data } = await api.get(`/api/appointments/booked-slots?doctorId=${docId}&date=${dateStr}`)
            if (data.success) {
                setBookedSlots(data.data)
            }
        } catch (err) {
            console.error("Failed to fetch booked slots", err)
        }
    }

    // ✅ Generate available slots
    const getAvailableSlots = () => {

        setDocSlots([])
        let today = new Date()

        for (let i = 0; i < 7; i++) {

            let currentDate = new Date(today)
            currentDate.setDate(today.getDate() + i)

            let endTime = new Date(today)
            endTime.setDate(today.getDate() + i)
            endTime.setHours(21, 0, 0, 0)

            if (today.getDate() === currentDate.getDate()) {
                currentDate.setHours(currentDate.getHours() > 10 ? currentDate.getHours() + 1 : 10)
                currentDate.setMinutes(currentDate.getMinutes() > 30 ? 30 : 0)
            } else {
                currentDate.setHours(10)
                currentDate.setMinutes(0)
            }

            let timeSlots = []

            while (currentDate < endTime) {

                let formattedTime = currentDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false })

                timeSlots.push({
                    datetime: new Date(currentDate),
                    time: formattedTime
                })

                currentDate.setMinutes(currentDate.getMinutes() + 30)
            }

            setDocSlots(prev => [...prev, timeSlots])
        }
    }

    // Fetch booked slots when slotIndex changes
    useEffect(() => {
        if (docSlots.length > 0 && docSlots[slotIndex] && docSlots[slotIndex][0]) {
            fetchBookedSlots(docSlots[slotIndex][0].datetime)
            setSlotTime('') // Reset selected time when date changes
        }
    }, [slotIndex, docSlots])

    // ✅ Book Appointment (API)
    const bookAppointment = async () => {
        if (!user) {
            toast.warning("Login first")
            return navigate('/login')
        }

        const { age, gender, phone } = patientInfo
        if (!age || !gender || !phone) {
            return toast.warning("Please fill your Age, Gender, and Phone Number")
        }

        const phoneRegex = /^[6-9]\d{9}$/
        if (!phoneRegex.test(phone)) {
            return toast.error("Please enter a valid 10-digit Indian phone number")
        }

        if (!slotTime) {
            return toast.warning("Please select time slot")
        }

        const date = docSlots[slotIndex][0].datetime
        const dateStr = new Date(date).toISOString().split('T')[0]
        const timeStr = `${slotTime}:00`

        try {
            await api.post("/api/appointments", {
                doctorId: docInfo.id,
                date: dateStr,
                time: timeStr,
                age: parseInt(age),
                gender: gender,
                phone: phone
            })
            toast.success("Appointment booked successfully")
            navigate('/my-appointments')
        } catch (err) {
            toast.error(err?.response?.data?.message || err.message)
        }
    }

    useEffect(() => {
        if (doctors.length > 0) {
            fetchDocInfo()
        }
    }, [doctors, docId])

    useEffect(() => {
        if (docInfo) {
            getAvailableSlots()
        }
    }, [docInfo])

    return docInfo ? (
        <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >

            {/* Doctor Info */}
            <div className='flex flex-col sm:flex-row gap-4'>

                <motion.div
                    initial={{ x: -40, opacity: 0 }}
                    animate={{ x: 0, opacity: 1 }}
                >
                    <img
                        className='bg-primary w-full sm:max-w-72 rounded-lg shadow object-cover h-72'
                        src={docInfo.imageUrl || assets.profile_pic}
                        alt={docInfo.name}
                    />
                </motion.div>

                <motion.div
                    initial={{ x: 40, opacity: 0 }}
                    animate={{ x: 0, opacity: 1 }}
                    className='flex-1 border border-[#ADADAD] rounded-lg p-8 bg-white mx-2 sm:mx-0 mt-[-80px] sm:mt-0'
                >

                    <p className='flex items-center gap-2 text-3xl font-medium text-gray-700'>
                        {docInfo.name}
                        <img className='w-5' src={assets.verified_icon} alt="" />
                    </p>

                    <div className='flex items-center gap-2 mt-1 text-gray-600'>
                        <p>{docInfo.specialization}</p>
                        <button className='py-0.5 px-2 border text-xs rounded-full'>
                            {docInfo.experience || "—"}
                        </button>
                    </div>

                    <div>
                        <p className='flex items-center gap-1 text-sm font-medium text-[#262626] mt-3'>
                            About <img className='w-3' src={assets.info_icon} alt="" />
                        </p>
                        <p className='text-sm text-gray-600 mt-1'>
                            {docInfo.department?.name ? `Department: ${docInfo.department.name}` : "—"}
                        </p>
                    </div>

                    <p className='text-gray-600 font-medium mt-4'>
                        Appointment fee:
                        <span className='text-gray-800'>
                            {currencySymbol}200
                        </span>
                    </p>

                </motion.div>
            </div>

            {/* Patient Information Form */}
            <div className='mt-10 p-6 border border-gray-200 rounded-2xl bg-gray-50/50 shadow-sm'>
                <p className='text-xl font-bold text-[#0f4c81] mb-6 flex items-center gap-2'>
                    <span className='bg-[#0f4c81] text-white w-8 h-8 rounded-full flex items-center justify-center text-sm'>1</span>
                    Patient Information
                </p>
                <div className='grid grid-cols-1 md:grid-cols-3 gap-6'>
                    <div className='flex flex-col gap-2'>
                        <label className='text-sm font-bold text-gray-600 ml-1'>Age</label>
                        <input 
                            type="number" 
                            placeholder='Enter Age'
                            className='border border-gray-300 rounded-xl px-4 py-3 focus:outline-none focus:border-[#0f4c81] transition bg-white'
                            value={patientInfo.age}
                            onChange={(e) => setPatientInfo({...patientInfo, age: e.target.value})}
                        />
                    </div>
                    <div className='flex flex-col gap-2'>
                        <label className='text-sm font-bold text-gray-600 ml-1'>Gender</label>
                        <select 
                            className='border border-gray-300 rounded-xl px-4 py-3 focus:outline-none focus:border-[#0f4c81] transition bg-white'
                            value={patientInfo.gender}
                            onChange={(e) => setPatientInfo({...patientInfo, gender: e.target.value})}
                        >
                            <option value="">Select Gender</option>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>
                    <div className='flex flex-col gap-2'>
                        <label className='text-sm font-bold text-gray-600 ml-1'>Phone Number</label>
                        <input 
                            type="tel" 
                            placeholder='Enter Phone Number'
                            className='border border-gray-300 rounded-xl px-4 py-3 focus:outline-none focus:border-[#0f4c81] transition bg-white'
                            value={patientInfo.phone}
                            onChange={(e) => setPatientInfo({...patientInfo, phone: e.target.value})}
                        />
                    </div>
                </div>
            </div>

            {/* Booking Slots */}
            <div className='mt-10 p-6 border border-gray-200 rounded-2xl'>
                <p className='text-xl font-bold text-[#0f4c81] mb-6 flex items-center gap-2'>
                    <span className='bg-[#0f4c81] text-white w-8 h-8 rounded-full flex items-center justify-center text-sm'>2</span>
                    Select Booking Slot
                </p>

                <div className='flex gap-3 w-full overflow-x-scroll mt-4 pb-2'>
                    {docSlots.map((item, index) => (
                        <motion.div
                            whileTap={{ scale: 0.95 }}
                            key={index}
                            onClick={() => setSlotIndex(index)}
                            className={`text-center py-6 min-w-20 rounded-2xl cursor-pointer transition-all duration-300
                            ${slotIndex === index ? 'bg-[#0f4c81] text-white shadow-lg' : 'border border-gray-200 bg-white hover:bg-gray-50'}`}
                        >
                            <p className='text-xs font-bold uppercase tracking-wider'>{item[0] && daysOfWeek[item[0].datetime.getDay()]}</p>
                            <p className='text-lg font-black mt-1'>{item[0] && item[0].datetime.getDate()}</p>
                        </motion.div>
                    ))}
                </div>

                <div className='flex gap-3 w-full overflow-x-scroll mt-6 pb-2'>
                    {docSlots.length > 0 &&
                        docSlots[slotIndex].map((item, index) => {
                            const isBooked = bookedSlots.includes(item.time);
                            return (
                                <motion.p
                                    whileTap={!isBooked ? { scale: 0.95 } : {}}
                                    key={index}
                                    onClick={() => !isBooked && setSlotTime(item.time)}
                                    className={`text-sm font-bold flex-shrink-0 
                                    px-6 py-3 rounded-xl transition-all duration-300
                                    ${isBooked 
                                        ? 'bg-gray-100 text-gray-300 border border-gray-200 cursor-not-allowed'
                                        : item.time === slotTime
                                            ? 'bg-[#0f4c81] text-white shadow-md'
                                            : 'text-gray-600 border border-gray-200 bg-white hover:border-[#0f4c81] cursor-pointer'
                                    }`}
                                >
                                    {isBooked ? 'Booked' : item.time.toLowerCase()}
                                </motion.p>
                            )
                        })}
                </div>

                <div className='flex justify-center md:justify-start'>
                    <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={bookAppointment}
                        className='bg-gradient-to-r from-[#0f4c81] to-[#2a7bbd] text-white px-20 py-4 rounded-2xl mt-10 shadow-xl font-bold uppercase tracking-widest'
                    >
                        Confirm Appointment
                    </motion.button>
                </div>
            </div>

            {/* Related Doctors */}
            <div className='mt-20'>
                <RelatedDoctors
                    speciality={docInfo.specialization}
                    docId={docId}
                />
            </div>

        </motion.div>
    ) : null
}

export default Appointment;